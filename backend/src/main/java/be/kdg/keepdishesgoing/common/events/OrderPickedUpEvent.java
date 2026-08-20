package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Received from the delivery service when a courier has collected the order
 * (US-29). Routing key: {@code delivery.<id>.order.pickedup.v1}.
 */
public record OrderPickedUpEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID restaurantId,
        UUID orderId
) implements DomainEvent {

    public OrderPickedUpEvent(UUID orderId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, orderId);
    }
}
