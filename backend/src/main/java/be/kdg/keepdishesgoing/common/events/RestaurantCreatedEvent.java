package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantCreatedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID restaurantId,
        UUID ownerId,
        String name,
        String cuisineType
) implements DomainEvent {
    public RestaurantCreatedEvent(UUID restaurantId, UUID ownerId, String name, String cuisineType) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, ownerId, name, cuisineType);
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}
