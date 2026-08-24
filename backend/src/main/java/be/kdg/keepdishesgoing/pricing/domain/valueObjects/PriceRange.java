package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

/**
 * How expensive a restaurant is, worked out from its average menu price (US-26).
 *
 * <p>Which band a price falls in is decided by {@link CriteriaVersion}.
 */
public enum PriceRange {
    CHEAP,
    REGULAR,
    EXPENSIVE,
    PREMIUM
}
