package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Enum representing all possible states of an Order
 */
public enum OrderStatus {
    /**
     * Checkout is done but the customer has not paid yet (US-23)
     * No restaurant has been told about this order
     */
    AWAITING_PAYMENT,

    /**
     * The customer's money is held, awaiting restaurant acceptance
     * Automatic rejection after 5 minutes if not decided
     */
    PENDING_ACCEPTANCE,

    /**
     * Restaurant has accepted the order and is preparing it
     */
    ACCEPTED,

    /**
     * Restaurant has rejected the order
     */
    REJECTED,

    /**
     * Checkout was started but never paid for (US-23)
     */
    PAYMENT_EXPIRED,

    /**
     * Order is prepared and ready for courier pickup
     */
    READY_FOR_PICKUP,

    /**
     * Courier has picked up the order and is delivering
     */
    IN_DELIVERY,

    /**
     * Order has been successfully delivered to customer
     */
    DELIVERED;

    public boolean isTerminal() {
        return this == REJECTED || this == DELIVERED || this == PAYMENT_EXPIRED;
    }

    /**
     * Whether a restaurant has been told about this order at all. Keeps unpaid
     * orders out of the owner's list and the busyness count (US-19).
     */
    public boolean isVisibleToRestaurant() {
        return this != AWAITING_PAYMENT && this != PAYMENT_EXPIRED;
    }

    public boolean canBeAccepted() {
        return this == PENDING_ACCEPTANCE;
    }

    public boolean canBeRejected() {
        return this == PENDING_ACCEPTANCE;
    }

    public boolean canBeMarkedReady() {
        return this == ACCEPTED;
    }

    public boolean canBePickedUp() {
        return this == READY_FOR_PICKUP;
    }

    public boolean canBeDelivered() {
        return this == IN_DELIVERY;
    }
}
