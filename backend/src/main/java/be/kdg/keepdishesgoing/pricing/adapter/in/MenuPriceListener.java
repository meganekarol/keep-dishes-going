package be.kdg.keepdishesgoing.pricing.adapter.in;

import be.kdg.keepdishesgoing.common.events.DishPublishedEvent;
import be.kdg.keepdishesgoing.common.events.DishUnpublishedEvent;
import be.kdg.keepdishesgoing.pricing.adapter.out.persistence.MenuPricePointJpaEntity;
import be.kdg.keepdishesgoing.pricing.adapter.out.persistence.MenuPricePointJpaRepository;
import be.kdg.keepdishesgoing.pricing.adapter.out.projections.MenuDishJpaEntity;
import be.kdg.keepdishesgoing.pricing.adapter.out.projections.MenuDishJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Writes down what a restaurant's menu averages, every time that changes (US-26).
 *
 * <p>The Restaurant context stores only the version of a dish that is live now,
 * so nothing there can say what the menu cost last month. This log is the only
 * record of it, and it starts the day a dish is first published.
 *
 * <p>These events are published internally via ApplicationEventPublisher (NOT RabbitMQ).
 */
@Component
public class MenuPriceListener {

    private static final Logger log = LoggerFactory.getLogger(MenuPriceListener.class);

    private final MenuDishJpaRepository dishRepository;
    private final MenuPricePointJpaRepository pricePointRepository;

    public MenuPriceListener(MenuDishJpaRepository dishRepository,
                             MenuPricePointJpaRepository pricePointRepository) {
        this.dishRepository = dishRepository;
        this.pricePointRepository = pricePointRepository;
    }

    /**
     * Fires on a repricing as well as a first publish, which is what makes
     * "the restaurant became more expensive" visible at all.
     */
    @EventListener
    @Transactional
    public void on(DishPublishedEvent event) {
        MenuDishJpaEntity dish = dishRepository.findById(event.dishId())
                .orElseGet(() -> new MenuDishJpaEntity(event.dishId(), event.restaurantId(), event.price()));
        dish.setPrice(event.price());
        dishRepository.save(dish);

        recordAverage(event.restaurantId(), event.occurredOn());
    }

    @EventListener
    @Transactional
    public void on(DishUnpublishedEvent event) {
        dishRepository.deleteById(event.dishId());
        recordAverage(event.restaurantId(), event.occurredOn());
    }

    private void recordAverage(UUID restaurantId, LocalDateTime at) {
        List<MenuDishJpaEntity> menu = dishRepository.findByRestaurantId(restaurantId);

        // An empty menu has no average, and recording a zero would draw the
        // restaurant as the cheapest on the platform. The line simply stops.
        if (menu.isEmpty()) {
            log.debug("Restaurant {} has nothing on the menu, no price point recorded", restaurantId);
            return;
        }

        BigDecimal total = menu.stream()
                .map(MenuDishJpaEntity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = total.divide(BigDecimal.valueOf(menu.size()), 2, RoundingMode.HALF_UP);

        pricePointRepository.save(new MenuPricePointJpaEntity(restaurantId, at, average));
        log.debug("Restaurant {} menu now averages {}", restaurantId, average);
    }
}
