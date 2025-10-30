package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantManuallyClosedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID restaurantId
) implements DomainEvent {
    public RestaurantManuallyClosedEvent(UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId);
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}
