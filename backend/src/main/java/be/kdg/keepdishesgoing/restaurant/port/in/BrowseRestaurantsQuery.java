package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSearchQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSummary;

import java.util.List;

/**
 * The customer's entry point into the catalogue (US-14, US-16).
 */
public interface BrowseRestaurantsQuery {

    /**
     * @param customerLocation where the browser says the customer is, or
     *                         {@code null} when they did not share it. Not a
     *                         filter: it only sharpens the delivery
     *                         guesstimate (US-19), and nothing is hidden for
     *                         being far away
     * @return every restaurant matching the query, ordered by name so the list
     *         does not reshuffle between two identical requests
     */
    List<RestaurantSummary> browse(RestaurantSearchQuery query, Coordinates customerLocation);
}
