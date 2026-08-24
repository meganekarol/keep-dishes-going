package be.kdg.keepdishesgoing.pricing.adapter.in.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The body of POST /api/price-range-criteria (US-32).
 *
 * <p>A date rather than an instant: the admin thinks in days, and thresholds
 * that start halfway through an afternoon would be an odd thing to ask for.
 *
 * @param effectiveFrom may be in the past
 */
public record AdjustPriceRangeCriteriaRequest(
        LocalDate effectiveFrom,
        BigDecimal cheapUnder,
        BigDecimal regularUnder,
        BigDecimal expensiveUnder) {
}
