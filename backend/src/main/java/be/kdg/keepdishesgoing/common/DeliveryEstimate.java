package be.kdg.keepdishesgoing.common;

/**
 * How long a customer is told to wait (US-19).
 *
 * <p>Shared kernel rather than a rule in either context. The catalogue quotes it
 * before an order exists and the order context quotes it afterwards, and two
 * copies of the arithmetic would eventually disagree in front of one customer.
 */
public final class DeliveryEstimate {

    /**
     * Average speed a courier is assumed to manage door to door, in km/h.
     * Low on purpose: the distance is measured straight over the ground, and
     * real roads are longer than that.
     */
    public static final double COURIER_SPEED_KMH = 25.0;

    /** Even next door takes a courier some minutes to collect and hand over. */
    public static final int MINIMUM_DELIVERY_MINUTES = 5;

    /**
     * Used when the distance cannot be worked out, which happens when either
     * side has no coordinates: the restaurant was never geocoded, or the
     * customer would not share their location.
     */
    public static final int ASSUMED_DELIVERY_MINUTES = 20;

    /**
     * Used when a restaurant's preparation time is unknown, which happens for
     * projections written before that field was published. Better a plausible
     * number than quoting a kitchen that takes an hour as if it took none.
     */
    public static final int ASSUMED_PREPARATION_MINUTES = 30;

    private DeliveryEstimate() {
    }

    /**
     * Minutes of driving for a straight-line distance.
     *
     * @param distanceKm as the crow flies, or {@code null} when unknown
     */
    public static int deliveryTime(Double distanceKm) {
        if (distanceKm == null || distanceKm.isNaN() || distanceKm < 0) {
            return ASSUMED_DELIVERY_MINUTES;
        }
        int minutes = (int) Math.ceil(distanceKm / COURIER_SPEED_KMH * 60);
        return Math.max(MINIMUM_DELIVERY_MINUTES, minutes);
    }

    /**
     * How much longer a queue makes the wait.
     *
     * <p>The count of orders not yet ready.
     * Taken literally that multiplies an idle restaurant's estimate by zero and
     * makes it instant, so the count is floored at one: a kitchen with nothing
     * on and a kitchen with one order both quote their plain estimate.
     */
    public static int busynessFactor(int ordersNotReady) {
        return Math.max(1, ordersNotReady);
    }

    /**
     * The whole formula.
     *
     * @param distanceKm         restaurant to customer, {@code null} when unknown
     * @param preparationMinutes the kitchen's own estimate, {@code 0} when unknown
     * @param ordersNotReady     orders this restaurant has still to cook
     */
    public static int totalMinutes(Double distanceKm, int preparationMinutes, int ordersNotReady) {
        int preparationTime = preparationMinutes > 0 ? preparationMinutes : ASSUMED_PREPARATION_MINUTES;
        return (deliveryTime(distanceKm) + preparationTime) * busynessFactor(ordersNotReady);
    }
}
