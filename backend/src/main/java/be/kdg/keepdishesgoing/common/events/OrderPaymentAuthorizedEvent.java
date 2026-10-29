package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registered when the payment provider confirms the customer's money is held
 * (US-23). Internal event, no {@code @Externalized}.
 *
 * <p>Makes the order visible to its restaurant.
 *
 * @param paymentReference what the provider calls this payment
 * @param amount           the sum held
 */
public record OrderPaymentAuthorizedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID orderId,
        UUID restaurantId,
        String paymentReference,
        BigDecimal amount
) implements DomainEvent {

    public OrderPaymentAuthorizedEvent(UUID orderId, UUID restaurantId,
                                       String paymentReference, BigDecimal amount) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, restaurantId, paymentReference, amount);
    }
}
