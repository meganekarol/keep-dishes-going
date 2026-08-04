package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSearchQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSummary;

import java.util.List;

/**
 * The customer's entry point into the catalogue (US-14, US-16).
 */
public interface BrowseRestaurantsUseCase {

    /**
     * @return every restaurant matching the query, ordered by name so the list
     *         does not reshuffle between two identical requests
     */
    List<RestaurantSummary> browse(RestaurantSearchQuery query);
}
