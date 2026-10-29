package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registered when a hold is given back to the customer without being taken
 * (US-23). Internal event, no {@code @Externalized}.
 *
 * <p>Happens when a restaurant turns the order down, and when nobody answers it
 * within five minutes.
 */
public record OrderPaymentReleasedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID orderId,
        UUID restaurantId,
        String paymentReference
) implements DomainEvent {

    public OrderPaymentReleasedEvent(UUID orderId, UUID restaurantId, String paymentReference) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, restaurantId, paymentReference);
    }
}
