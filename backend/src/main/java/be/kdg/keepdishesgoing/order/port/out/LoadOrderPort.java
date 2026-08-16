package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for loading an Order aggregate from persistence.
 * Implemented by the persistence adapter (event store + snapshot).
 */
public interface LoadOrderPort {
    Optional<Order> loadBy(OrderId orderId);

    /**
     * Every order the event store has ever heard of.
     *
     * <p>For rebuilding the read model, which is the one job that has to visit
     * all of them. Nothing on a request path should call this.
     */
    List<UUID> findAllOrderIds();
}
