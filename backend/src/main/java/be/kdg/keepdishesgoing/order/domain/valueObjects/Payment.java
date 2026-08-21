package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.time.LocalDateTime;

/**
 * What the payment provider is doing with this order's money (US-23).
 *
 * <p>Immutable: each step returns a new instance, so replaying the events that
 * produced it always arrives at the same answer.
 *
 * @param reference   the provider's own id for this payment, {@code null} until
 *                    the customer completes checkout
 * @param status      where the money is
 * @param authorizedAt when the hold was placed
 * @param capturedAt  when the money was taken, {@code null} if it never was
 * @param releasedAt  when the hold was given back, {@code null} if it was not
 */
public record Payment(
        String reference,
        PaymentStatus status,
        LocalDateTime authorizedAt,
        LocalDateTime capturedAt,
        LocalDateTime releasedAt
) {

    /** An order that has just been checked out: nothing has been paid yet. */
    public static Payment pending() {
        return new Payment(null, PaymentStatus.PENDING, null, null, null);
    }

    public Payment authorized(String reference, LocalDateTime at) {
        return new Payment(reference, PaymentStatus.AUTHORIZED, at, null, null);
    }

    public Payment captured(LocalDateTime at) {
        return new Payment(reference, PaymentStatus.CAPTURED, authorizedAt, at, null);
    }

    public Payment released(LocalDateTime at) {
        return new Payment(reference, PaymentStatus.RELEASED, authorizedAt, null, at);
    }

    public boolean isHeld() {
        return status.isHeld();
    }

    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED;
    }
}
