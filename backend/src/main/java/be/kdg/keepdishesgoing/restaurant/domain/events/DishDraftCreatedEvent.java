package be.kdg.keepdishesgoing.restaurant.domain.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record DishDraftCreatedEvent(
        UUID eventId,
        UUID dishId,
        UUID restaurantId,
        String dishName,
        LocalDateTime occurredOn
) implements DomainEvent {

    public DishDraftCreatedEvent(UUID dishId, UUID restaurantId,String dishName) {
        this(UUID.randomUUID(), dishId, restaurantId, dishName, LocalDateTime.now());
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}
