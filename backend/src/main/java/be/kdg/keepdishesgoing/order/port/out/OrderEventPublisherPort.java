package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.util.List;

/**
 * Output port for publishing domain events from the Order Context.
 * Used for integration with RabbitMQ or an Outbox pattern.
 */

public interface OrderEventPublisherPort {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> event);
}
