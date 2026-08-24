package be.kdg.keepdishesgoing.pricing.port.in;

import be.kdg.keepdishesgoing.pricing.port.in.queries.PriceRangeEvolution;

import java.util.UUID;

/**
 * How a restaurant's price range moved over time (US-26).
 */
public interface ViewPriceRangeEvolutionQuery {

    PriceRangeEvolution evolutionOf(UUID restaurantId);
}
