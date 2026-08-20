package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.common.events.OrderAcceptedEvent;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.AcceptOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.AcceptOrderCommand;
import be.kdg.keepdishesgoing.order.port.out.*;
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
    private final OrderEventPublisherPort orderEventPublisherPort;
    private final LoadRestaurantProjectionPort loadRestaurantProjectionPort;

    public AcceptOrderUseCaseImpl(
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            OrderEventPublisherPort orderEventPublisherPort, OrderEventPublisherPort orderEventPublisherPort1,
            LoadRestaurantProjectionPort loadRestaurantProjectionPort) {
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.orderEventPublisherPort = orderEventPublisherPort1;
        this.loadRestaurantProjectionPort = loadRestaurantProjectionPort;
    }

    @Override
    public void acceptOrder(AcceptOrderCommand command) {
        log.info("Restaurant {} accepting order {}", command.restaurantId(), command.orderId());

        // Load order from event store
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrderPort.loadBy(orderId).orElseThrow();

        // Validate restaurant owns the order
        validateRestaurantOwnership(order, command.restaurantId());

        // The courier needs collecting from somewhere, and the order only knows
        // which restaurant it belongs to. Looked up here rather than inside the
        // aggregate, which has no business reaching for another context's data.
        var pickup = loadRestaurantProjectionPort.getPickupPoint(command.restaurantId());

        order.accept(
                new OrderAcceptedEvent.Address(
                        pickup.street(), pickup.number(), pickup.postalCode(), pickup.city()),
                pickup.hasCoordinates()
                        ? new OrderAcceptedEvent.Point(pickup.latitude(), pickup.longitude())
                        : null);

        // Save events
        updateOrderPort.save(order);

        // Publish events (includes OrderAcceptedEvent with @Externalized to RabbitMQ)
        orderEventPublisherPort.publishAll(order.getUncommittedEvents());
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
