package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.Order;

/**
 * Output port for saving an Order aggregate and its new events.
 * Implemented by the persistence adapter (event store + snapshot).
 */
public interface SaveOrderPort {
    void save(Order order);
    void saveSnapshot(Order order);
}
