package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;

import java.util.Optional;

/**
 * Output port for loading an Order aggregate from persistence.
 * Implemented by the persistence adapter (event store + snapshot).
 */
public interface LoadOrderPort {
    Optional<Order> loadBy(OrderId orderId);
}
