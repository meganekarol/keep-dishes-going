package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Integration event - received from external delivery service via RabbitMQ.
 * Published when courier picks up the order from restaurant.
 * Routing key: delivery.{orderId}.order.pickedup.v1
 * 
 * This event is NOT published by us (no @Externalized).
 * It's received from RabbitMQ via @RabbitListener.
 */
public record OrderPickedUpEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID orderId,
        UUID courierId,
        String courierName
) implements DomainEvent {
    
    public OrderPickedUpEvent(UUID orderId, UUID courierId, String courierName) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, courierId, courierName);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}