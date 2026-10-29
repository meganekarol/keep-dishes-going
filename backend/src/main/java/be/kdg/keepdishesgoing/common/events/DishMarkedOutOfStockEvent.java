package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal domain event - published when a dish is temporarily out of stock.
 * Order context listens to update dish status in projection.
 * NOT sent to RabbitMQ.
 */
public record DishMarkedOutOfStockEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID dishId,
        UUID restaurantId,
        String dishName
) implements DomainEvent {
    
    public DishMarkedOutOfStockEvent(UUID dishId, UUID restaurantId, String dishName) {
        this(UUID.randomUUID(), LocalDateTime.now(), dishId, restaurantId, dishName);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}