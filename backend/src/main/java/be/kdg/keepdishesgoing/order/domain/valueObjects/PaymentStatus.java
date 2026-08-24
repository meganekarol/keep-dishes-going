package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Where an order's money is (US-23).
 *
 * <p>Kept apart from {@link OrderStatus} because the two move at different times.
 */
public enum PaymentStatus {

    /** Checkout has started and the customer has not come back from the provider. */
    PENDING,

    /** The provider is holding the money. It has not left the customer yet. */
    AUTHORIZED,

    /** The money has been taken. This is the only state KDG has been paid in. */
    CAPTURED,

    /** The hold was given back without being taken. */
    RELEASED;

    /**
     * Checks whether the money is being held.
     *
     * @return {@code true} if the hold can still be taken or given back
     */
    public boolean isHeld() {
        return this == AUTHORIZED;
    }
}
