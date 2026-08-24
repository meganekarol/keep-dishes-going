package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One set of price range thresholds and the moment it starts to count (US-32).
 *
 * <p>The assignment writes the bands with gaps in them (nothing is 10 to 11).
 * Three rising thresholds close those gaps, so every average lands in exactly
 * one band.
 *
 * @param effectiveFrom may be in the past: the admin corrects history as well
 *                      as setting it
 */
public record CriteriaVersion(
        LocalDateTime effectiveFrom,
        BigDecimal cheapUnder,
        BigDecimal regularUnder,
        BigDecimal expensiveUnder) {

    /** What the platform started with, used for any time before the admin set anything. */
    public static final CriteriaVersion INITIAL = new CriteriaVersion(
            LocalDateTime.MIN,
            new BigDecimal("10"),
            new BigDecimal("30"),
            new BigDecimal("60"));

    public CriteriaVersion {
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("Criteria must say when they take effect");
        }
        if (cheapUnder == null || regularUnder == null || expensiveUnder == null) {
            throw new IllegalArgumentException("All three thresholds are required");
        }
        if (cheapUnder.signum() <= 0) {
            throw new IllegalArgumentException("Thresholds must be above zero");
        }
        if (cheapUnder.compareTo(regularUnder) >= 0 || regularUnder.compareTo(expensiveUnder) >= 0) {
            throw new IllegalArgumentException(
                    "Thresholds must rise: cheap, then regular, then expensive");
        }
    }

    /** Which band an average menu price falls in. */
    public PriceRange classify(BigDecimal averageMenuPrice) {
        if (averageMenuPrice.compareTo(cheapUnder) < 0) {
            return PriceRange.CHEAP;
        }
        if (averageMenuPrice.compareTo(regularUnder) < 0) {
            return PriceRange.REGULAR;
        }
        if (averageMenuPrice.compareTo(expensiveUnder) < 0) {
            return PriceRange.EXPENSIVE;
        }
        return PriceRange.PREMIUM;
    }
}
