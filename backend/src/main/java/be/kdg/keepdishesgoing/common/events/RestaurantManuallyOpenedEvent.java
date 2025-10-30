package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantManuallyOpenedEvent(
        UUID eventId,
        LocalDateTime occuredAt,
        UUID restaurantId
) implements DomainEvent {

    public RestaurantManuallyOpenedEvent(UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId);
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}

