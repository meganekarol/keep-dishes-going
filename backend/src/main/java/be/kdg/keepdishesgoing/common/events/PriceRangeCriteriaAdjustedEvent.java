package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal domain event - published when the platform admin sets new price
 * range thresholds (US-32).
 *
 * <p>Two different times: {@code effectiveFrom} is when the thresholds start to
 * count and may be in the past, {@code occurredOn} is when the admin said so.
 */
public record PriceRangeCriteriaAdjustedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        LocalDateTime effectiveFrom,
        BigDecimal cheapUnder,
        BigDecimal regularUnder,
        BigDecimal expensiveUnder
) implements DomainEvent {

    public PriceRangeCriteriaAdjustedEvent(LocalDateTime effectiveFrom, BigDecimal cheapUnder,
                                           BigDecimal regularUnder, BigDecimal expensiveUnder) {
        this(UUID.randomUUID(), LocalDateTime.now(), effectiveFrom,
             cheapUnder, regularUnder, expensiveUnder);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}
