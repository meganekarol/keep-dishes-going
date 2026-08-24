package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Where an order is, from checkout to the customer's door.
 *
 * <p>The rules about which step can follow which live here rather than in a
 * service, so there is one place to look.
 */
public enum OrderStatus {

    /** Checkout is done but the customer has not paid yet. No restaurant knows about it (US-23). */
    AWAITING_PAYMENT,

    /** The money is held and the restaurant has to decide. Declined automatically after five minutes. */
    PENDING_ACCEPTANCE,

    /** The restaurant took the order on and is cooking it. */
    ACCEPTED,

    /** The restaurant turned the order away. */
    REJECTED,

    /** Checkout was started but never paid for (US-23). */
    PAYMENT_EXPIRED,

    /** The food is cooked and waiting for a courier. */
    READY_FOR_PICKUP,

    /** A courier has the food and is on the way. */
    IN_DELIVERY,

    /** The food reached the customer. */
    DELIVERED;

    /**
     * Checks whether the order is finished and cannot move again.
     *
     * @return {@code true} if the order was rejected, delivered, or never paid for
     */
    public boolean isTerminal() {
        return this == REJECTED || this == DELIVERED || this == PAYMENT_EXPIRED;
    }

    /**
     * Checks whether the restaurant has been told about this order at all.
     *
     * <p>Keeps unpaid orders out of the owner's list and out of the busyness
     * count used for delivery estimates (US-19).
     *
     * @return {@code true} if the order is one the restaurant can see
     */
    public boolean isVisibleToRestaurant() {
        return this != AWAITING_PAYMENT && this != PAYMENT_EXPIRED;
    }

    /**
     * Checks whether the restaurant can still accept this order.
     *
     * @return {@code true} if it is waiting for a decision
     */
    public boolean canBeAccepted() {
        return this == PENDING_ACCEPTANCE;
    }

    /**
     * Checks whether the restaurant can still reject this order.
     *
     * @return {@code true} if it is waiting for a decision
     */
    public boolean canBeRejected() {
        return this == PENDING_ACCEPTANCE;
    }

    /**
     * Checks whether the order can be marked ready for a courier.
     *
     * @return {@code true} if the restaurant has accepted it
     */
    public boolean canBeMarkedReady() {
        return this == ACCEPTED;
    }

    /**
     * Checks whether a courier can pick the order up.
     *
     * @return {@code true} if it is waiting for pickup
     */
    public boolean canBePickedUp() {
        return this == READY_FOR_PICKUP;
    }

    /**
     * Checks whether the order can be marked delivered.
     *
     * @return {@code true} if a courier is out with it
     */
    public boolean canBeDelivered() {
        return this == IN_DELIVERY;
    }
}
