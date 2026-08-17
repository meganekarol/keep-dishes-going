package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.common.events.OrderPlacedEvent;
import be.kdg.keepdishesgoing.common.events.OrderReadyEvent;
import be.kdg.keepdishesgoing.common.events.OrderRejectedEvent;
import be.kdg.keepdishesgoing.restaurant.adapter.out.projections.RestaurantBusynessJpaEntity;
import be.kdg.keepdishesgoing.restaurant.adapter.out.projections.RestaurantBusynessJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Counts what is sitting in each kitchen, for the delivery guesstimate (US-19).
 *
 * <p>The mirror of the order context's {@code RestaurantProjectionListener}:
 * each context keeps its own read model of what the other one owns, so neither
 * has to call into the other.
 *
 * <p>Only three events move the number. An order joins the queue when it is
 * placed and leaves it when the kitchen marks it ready or turns it away. Being
 * picked up and delivered both happen after ready, so they change nothing here.
 */
@Component
public class OrderActivityListener {

    private static final Logger log = LoggerFactory.getLogger(OrderActivityListener.class);

    private final RestaurantBusynessJpaRepository busynessRepository;

    public OrderActivityListener(RestaurantBusynessJpaRepository busynessRepository) {
        this.busynessRepository = busynessRepository;
    }

    @EventListener
    @Transactional
    public void on(OrderPlacedEvent event) {
        RestaurantBusynessJpaEntity busyness = busynessRepository.findById(event.restaurantId())
                .orElseGet(() -> new RestaurantBusynessJpaEntity(event.restaurantId()));
        busyness.orderArrived();
        busynessRepository.save(busyness);
        log.debug("Restaurant {} now has {} order(s) to cook",
                event.restaurantId(), busyness.getOrdersNotReady());
    }

    @EventListener
    @Transactional
    public void on(OrderReadyEvent event) {
        orderLeftTheKitchen(event.restaurantId());
    }

    @EventListener
    @Transactional
    public void on(OrderRejectedEvent event) {
        orderLeftTheKitchen(event.restaurantId());
    }

    private void orderLeftTheKitchen(UUID restaurantId) {
        busynessRepository.findById(restaurantId).ifPresent(busyness -> {
            busyness.orderLeftTheKitchen();
            busynessRepository.save(busyness);
            log.debug("Restaurant {} now has {} order(s) to cook",
                    restaurantId, busyness.getOrdersNotReady());
        });
    }
}
