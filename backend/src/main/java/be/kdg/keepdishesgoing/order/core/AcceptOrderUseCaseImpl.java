package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.AcceptOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.AcceptOrderCommand;
import be.kdg.keepdishesgoing.order.port.out.*;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case implementation for restaurant to accept an order
 */
@Service
@Transactional
public class AcceptOrderUseCaseImpl implements AcceptOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(AcceptOrderUseCaseImpl.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort updateOrderPort;
    private final PublishEventPort publishEventPort;

    public AcceptOrderUseCaseImpl(
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            PublishEventPort publishEventPort) {
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void acceptOrder(AcceptOrderCommand command) {
        log.info("Restaurant {} accepting order {}", command.restaurantId(), command.orderId());

        // Load order from event store
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrderPort.loadBy(orderId).orElseThrow();

        // Validate restaurant owns the order
        validateRestaurantOwnership(order, command.restaurantId());

        // Accept the order (domain logic)
        order.accept();

        // Save events
        updateOrderPort.save(order);

        // Publish events (includes OrderAcceptedEvent with @Externalized to RabbitMQ)
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Order {} accepted successfully by restaurant {}", command.orderId(), command.restaurantId());
    }

    private void validateRestaurantOwnership(Order order, UUID restaurantId) {
        UUID orderRestaurantId = order.getRestaurantId().uuid();
        if (!orderRestaurantId.equals(restaurantId)) {
            log.error("Restaurant {} attempted to accept order {} belonging to restaurant {}",
                    restaurantId, order.getOrderId(), orderRestaurantId);
            throw new IllegalArgumentException("Restaurant does not own this order");
        }
    }
}
