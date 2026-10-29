package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Carries the weekly schedule so that other contexts can evaluate opening hours
 * on their own. The schedule is fixed at creation, so this is the only event
 * that needs to publish it.
 *
 * @param defaultPreparationTime minutes the kitchen asks for, travelling with
 *                               the schedule for the same reason: the order
 *                               context quotes a delivery time (US-19) and
 *                               cannot reach into the restaurant's tables to
 *                               look it up
 * @param latitude               where the restaurant is, or {@code null} when
 *                               geocoding it failed. Travels for the same
 *                               reason again: an accepted order is published
 *                               to the delivery service with this as the
 *                               pickup point (US-28)
 */
public record RestaurantCreatedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID restaurantId,
        UUID ownerId,
        String name,
        String cuisineType,
        int defaultPreparationTime,
        String street,
        String number,
        String postalCode,
        String city,
        Double latitude,
        Double longitude,
        List<OpeningHoursSlot> openingHours
) implements DomainEvent {

    public RestaurantCreatedEvent(UUID restaurantId, UUID ownerId, String name, String cuisineType,
                                  int defaultPreparationTime, String street, String number,
                                  String postalCode, String city, Double latitude, Double longitude,
                                  List<OpeningHoursSlot> openingHours) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, ownerId, name, cuisineType,
                defaultPreparationTime, street, number, postalCode, city, latitude, longitude,
                List.copyOf(openingHours));
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}
