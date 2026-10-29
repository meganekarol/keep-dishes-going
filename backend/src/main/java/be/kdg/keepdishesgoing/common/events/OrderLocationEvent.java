package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Received repeatedly while a courier is en route (US-29).
 * Routing key: {@code delivery.<id>.order.location.v1}.
 */
public record OrderLocationEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID restaurantId,
        UUID orderId,
        Double lat,
        Double lng,
        Point location
) implements DomainEvent {

    /** The nested form, as the specification describes it. */
    public record Point(double lat, double lng) {
    }

    /** @return the reported latitude whichever shape it arrived in, or null */
    public Double latitude() {
        if (lat != null) return lat;
        return location == null ? null : location.lat();
    }

    /** @return the reported longitude whichever shape it arrived in, or null */
    public Double longitude() {
        if (lng != null) return lng;
        return location == null ? null : location.lng();
    }

    public boolean hasPosition() {
        return latitude() != null && longitude() != null;
    }
}
