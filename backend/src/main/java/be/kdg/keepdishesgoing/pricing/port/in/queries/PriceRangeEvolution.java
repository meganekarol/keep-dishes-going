package be.kdg.keepdishesgoing.pricing.port.in.queries;

import java.util.List;
import java.util.UUID;

/**
 * How a restaurant's price range moved over time (US-26).
 *
 * @param points empty when nothing has been recorded for this restaurant yet
 */
public record PriceRangeEvolution(UUID restaurantId, List<PriceRangePoint> points) {
}
