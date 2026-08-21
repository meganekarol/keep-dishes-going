package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Where an order's money is (US-23).
 *
 * <p>Separate from {@link OrderStatus} because the two move on different clocks;
 * combining them would need a state per combination.
 */
public enum PaymentStatus {

    /** Checkout has started, the customer has not come back from the provider. */
    PENDING,

    /** The provider is holding the money. It has not left the customer yet. */
    AUTHORIZED,

    /** The money has been taken. This is the only state KDG has been paid in. */
    CAPTURED,

    /** The hold was given back without being taken. */
    RELEASED;

    /** Only an authorized hold can be taken or given back. */
    public boolean isHeld() {
        return this == AUTHORIZED;
    }
}
