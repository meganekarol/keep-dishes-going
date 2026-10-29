package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * Whether a restaurant is taking orders (US-09).
 *
 * <p>{@link #OPEN} and {@link #CLOSED} follow the opening hours. The two manual
 * values are an owner overriding those hours.
 */
public enum RestaurantStatus {
    OPEN,
    CLOSED,
    MANUALLY_OPENED,
    MANUALLY_CLOSED
}
