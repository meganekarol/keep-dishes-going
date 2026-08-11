package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The owner handed control back to the weekly schedule, dropping an earlier
 * manual open or close (US-09).
 *
 * <p>Without this, an override lasts forever: a restaurant that closed early one
 * evening would still be closed the following week, because a manual state
 * short-circuits the schedule wherever openness is evaluated.
 */
public record RestaurantScheduleResumedEvent(
        UUID eventId,
        LocalDateTime occuredAt,
        UUID restaurantId
) implements DomainEvent {

    public RestaurantScheduleResumedEvent(UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occuredAt;
    }
}
