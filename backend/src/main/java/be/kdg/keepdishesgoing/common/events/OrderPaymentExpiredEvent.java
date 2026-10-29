package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registered when a checkout was started but never paid for (US-23). Internal
 * event, no {@code @Externalized}.
 *
 * <p>Keeps abandoned checkouts out of the restaurant's busyness count (US-19).
 */
public record OrderPaymentExpiredEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID orderId,
        UUID restaurantId
) implements DomainEvent {

    public OrderPaymentExpiredEvent(UUID orderId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, restaurantId);
    }
}
