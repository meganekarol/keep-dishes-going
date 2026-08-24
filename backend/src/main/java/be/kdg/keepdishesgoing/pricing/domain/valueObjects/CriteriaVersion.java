package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One set of price range thresholds and the date it starts to count (US-32).
 *
 * @param effectiveFrom  when these thresholds start to count, which may be in the past
 * @param cheapUnder     an average below this is cheap
 * @param regularUnder   an average below this is regular
 * @param expensiveUnder an average below this is expensive, and anything higher is premium
 */
public record CriteriaVersion(
        LocalDateTime effectiveFrom,
        BigDecimal cheapUnder,
        BigDecimal regularUnder,
        BigDecimal expensiveUnder) {

    /** The thresholds the platform started with, used for any time before the admin set any. */
    public static final CriteriaVersion INITIAL = new CriteriaVersion(
            LocalDateTime.MIN,
            new BigDecimal("10"),
            new BigDecimal("30"),
            new BigDecimal("60"));

    /**
     * Checks the thresholds make sense.
     *
     * @throws IllegalArgumentException if a threshold is missing, is zero or below,
     *                                  or if the three do not rise in order
     */
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

    /**
     * Works out which band an average menu price falls in.
     *
     * @param averageMenuPrice the restaurant's average menu price
     * @return the matching price range
     */
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
