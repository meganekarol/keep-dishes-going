package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event - received from external delivery service via RabbitMQ.
 * Published when courier delivers the order to customer.
 * Routing key: delivery.{orderId}.order.delivered.v1
 *
 * This event is NOT published by us (no @Externalized).
 * It's received from RabbitMQ via @RabbitListener.
 */
public record OrderDeliveredEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID orderId,
        LocalDateTime deliveredAt
) implements DomainEvent {

    public OrderDeliveredEvent(UUID orderId, LocalDateTime deliveredAt) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, deliveredAt);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}