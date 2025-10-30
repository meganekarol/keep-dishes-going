package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Enum representing all possible states of an Order
 */
public enum OrderStatus {
    /**
     * Order has been placed, awaiting restaurant acceptance
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
        return this == REJECTED || this == DELIVERED;
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