package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.time.LocalDateTime;

/**
 * What the payment provider is doing with this order's money (US-23).
 *
 * <p>Every step returns a new instance rather than changing this one.
 *
 * @param reference    the provider's own id for this payment, {@code null} until
 *                     the customer completes checkout
 * @param status       where the money is
 * @param authorizedAt when the hold was placed
 * @param capturedAt   when the money was taken, {@code null} if it never was
 * @param releasedAt   when the hold was given back, {@code null} if it was not
 */
public record Payment(
        String reference,
        PaymentStatus status,
        LocalDateTime authorizedAt,
        LocalDateTime capturedAt,
        LocalDateTime releasedAt
) {

    /**
     * Builds the payment of an order that has just been checked out.
     *
     * @return a payment with nothing paid yet
     */
    public static Payment pending() {
        return new Payment(null, PaymentStatus.PENDING, null, null, null);
    }

    /**
     * Records that the provider is now holding the money.
     *
     * @param reference the provider's id for this payment
     * @param at        when the hold was placed
     * @return a new payment in the authorized state
     */
    public Payment authorized(String reference, LocalDateTime at) {
        return new Payment(reference, PaymentStatus.AUTHORIZED, at, null, null);
    }

    /**
     * Records that the money has been taken.
     *
     * @param at when it was taken
     * @return a new payment in the captured state
     */
    public Payment captured(LocalDateTime at) {
        return new Payment(reference, PaymentStatus.CAPTURED, authorizedAt, at, null);
    }

    /**
     * Records that the hold was given back without being taken.
     *
     * @param at when it was given back
     * @return a new payment in the released state
     */
    public Payment released(LocalDateTime at) {
        return new Payment(reference, PaymentStatus.RELEASED, authorizedAt, null, at);
    }

    /**
     * Checks whether the money is being held.
     *
     * @return {@code true} if the provider is holding it
     */
    public boolean isHeld() {
        return status.isHeld();
    }

    /**
     * Checks whether the money has been taken.
     *
     * @return {@code true} if it has been captured
     */
    public boolean isCaptured() {
        return status == PaymentStatus.CAPTURED;
    }
}
