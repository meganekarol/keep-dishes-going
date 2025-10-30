package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal domain event - published when a dish is removed from the menu.
 * Order context listens to remove the dish from its projection.
 * NOT sent to RabbitMQ.
 */
public record DishUnpublishedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID dishId,
        UUID restaurantId
) implements DomainEvent {
    
    public DishUnpublishedEvent(UUID dishId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), dishId, restaurantId);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}