package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registered when the held money is actually taken, which happens as the
 * restaurant accepts (US-23). Internal event, no {@code @Externalized}.
 *
 * <p>Kept separate from {@link OrderAcceptedEvent}: being charged and being
 * accepted are two different facts.
 */
public record OrderPaymentCapturedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID orderId,
        UUID restaurantId,
        String paymentReference,
        BigDecimal amount
) implements DomainEvent {

    public OrderPaymentCapturedEvent(UUID orderId, UUID restaurantId,
                                     String paymentReference, BigDecimal amount) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, restaurantId, paymentReference, amount);
    }
}
