package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Received from the delivery service when the food has reached the customer
 * (US-29). Routing key: {@code delivery.<id>.order.delivered.v1}..
 */
public record OrderDeliveredEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID restaurantId,
        UUID orderId
) implements DomainEvent {

    public OrderDeliveredEvent(UUID orderId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, orderId);
    }
}
