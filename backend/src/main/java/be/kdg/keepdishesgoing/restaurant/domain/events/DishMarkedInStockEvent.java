package be.kdg.keepdishesgoing.restaurant.domain.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record DishMarkedInStockEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID dishId,
        UUID restaurantId
) implements DomainEvent {
    public DishMarkedInStockEvent(UUID dishId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), dishId, restaurantId);
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}
