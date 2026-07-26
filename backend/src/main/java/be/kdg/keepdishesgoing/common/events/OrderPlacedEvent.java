package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Event registered when a customer places a new order.
 * Internal event - NO @Externalized (stays within the system).
 *
 * <p>This is a "fat" event: it carries the full initial state of the Order
 * aggregate so that the aggregate can be reconstructed from a blank instance
 * by replaying events (event sourcing). It is kept primitive on purpose so the
 * shared kernel does not depend on the Order context's domain value objects.
 */
public record OrderPlacedEvent(
    UUID eventId,
    LocalDateTime occurredOn,
    UUID orderId,
    UUID restaurantId,
    String customerName,
    String contactEmail,
    String street,
    String number,
    String postalCode,
    String city,
    String country,
    List<LineItem> lines,
    BigDecimal totalPrice
) implements DomainEvent {

    /**
     * A single ordered dish, captured at the moment of ordering.
     */
    public record LineItem(
        UUID dishId,
        String dishName,
        BigDecimal unitPrice,
        int quantity
    ) {}

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}
