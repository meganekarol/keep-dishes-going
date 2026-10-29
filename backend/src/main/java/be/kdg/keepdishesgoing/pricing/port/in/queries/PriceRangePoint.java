package be.kdg.keepdishesgoing.pricing.port.in.queries;

import be.kdg.keepdishesgoing.pricing.domain.valueObjects.PriceRange;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One step on the evolution graph (US-26).
 *
 * <p>Carries the average as well as the band, so the graph can say why it
 * stepped: the menu got dearer, or the thresholds moved under it.
 */
public record PriceRangePoint(
        LocalDateTime at,
        BigDecimal averageMenuPrice,
        PriceRange priceRange) {
}
