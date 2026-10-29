package be.kdg.keepdishesgoing.pricing.port.in.commands;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * New thresholds for the price ranges (US-32).
 *
 * @param effectiveFrom when they start to count, which may be in the past
 */
public record AdjustPriceRangeCriteriaCommand(
        LocalDateTime effectiveFrom,
        BigDecimal cheapUnder,
        BigDecimal regularUnder,
        BigDecimal expensiveUnder) {
}
