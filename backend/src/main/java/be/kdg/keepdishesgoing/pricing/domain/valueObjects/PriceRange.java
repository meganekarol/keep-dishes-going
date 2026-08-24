package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

/**
 * How expensive a restaurant is, from its average menu price (US-26).
 *
 * <p>The band alone means nothing without the thresholds that produced it: the
 * platform admin can move those, so the same menu can change band without
 * changing a single price. See {@link CriteriaVersion}.
 */
public enum PriceRange {
    CHEAP,
    REGULAR,
    EXPENSIVE,
    PREMIUM
}
