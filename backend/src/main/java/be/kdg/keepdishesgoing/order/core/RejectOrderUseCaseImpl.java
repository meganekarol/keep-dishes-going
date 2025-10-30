package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.OrderId;
import be.kdg.keepdishesgoing.order.port.in.RejectOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.RejectOrderCommand;
import be.kdg.keepdishesgoing.order.port.out.*;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class RejectOrderUseCaseImpl implements RejectOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(RejectOrderUseCaseImpl.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort updateOrderPort;
    private final PublishEventPort publishEventPort;

    public RejectOrderUseCaseImpl(
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            PublishEventPort publishEventPort) {
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void rejectOrder(RejectOrderCommand command) {
        log.info("Restaurant {} rejecting order {} with reason: {}",
                command.restaurantId(), command.orderId(), command.reason());

        // Load order from event store
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrderPort.loadBy(orderId).orElseThrow();

        // Validate restaurant owns the order
        validateRestaurantOwnership(order, command.restaurantId());

        // Reject the order (domain logic)
        order.reject(command.reason());

        // Save events
        updateOrderPort.save(order);

        // Publish events
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Order {} rejected successfully by restaurant {}", command.orderId(), command.restaurantId());
    }

    private void validateRestaurantOwnership(Order order, UUID restaurantId) {
        UUID orderRestaurantId = order.getRestaurantId().uuid();
        if (!orderRestaurantId.equals(restaurantId)) {
            log.error("Restaurant {} attempted to reject order {} belonging to restaurant {}",
                    restaurantId, order.getOrderId(), orderRestaurantId);
            throw new IllegalArgumentException("Restaurant does not own this order");
        }
    }
}
