package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when a restaurant takes an order on (US-28).
 *
 * <p>This is what dispatches a courier, which is why it carries both ends of
 * the journey. The restaurant's half is looked up when the order is accepted;
 * the customer's half is geocoded at checkout.
 */
@Externalized("kdg.events::#{'restaurant.' + #this.restaurantId() + '.order.accepted.v1'}")
public record OrderAcceptedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID restaurantId,
        UUID orderId,
        Address pickupAddress,
        Point pickupCoordinates,
        Address dropoffAddress,
        Point dropoffCoordinates
) implements DomainEvent {
    public record Address(String street, String number, String postalCode, String city) {
    }

    public record Point(double lat, double lng) {
    }

    public OrderAcceptedEvent(UUID orderId, UUID restaurantId,
                              Address pickupAddress, Point pickupCoordinates,
                              Address dropoffAddress, Point dropoffCoordinates) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, orderId,
                pickupAddress, pickupCoordinates, dropoffAddress, dropoffCoordinates);
    }
}
