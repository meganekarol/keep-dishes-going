package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.DishAvailability;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.DishSearchQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.MenuItem;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantDetail;

import java.util.List;
import java.util.UUID;

/**
 * One restaurant's page and its menu, as a customer sees them
 * (US-15, US-17, US-18).
 */
public interface ViewRestaurantMenuQuery {

    /**
     * @throws be.kdg.keepdishesgoing.common.exceptions.NotFoundException
     *         if no such restaurant exists
     */
    RestaurantDetail viewRestaurant(UUID restaurantId, Coordinates customerLocation);

    /**
     * The same menu, filtered and sorted. Split from
     * {@link #viewRestaurant} because a customer narrowing the menu should
     * not re-fetch the restaurant's address and schedule on every keystroke.
     *
     * @throws be.kdg.keepdishesgoing.common.exceptions.NotFoundException
     *         if no such restaurant exists
     */
    List<MenuItem> browseMenu(DishSearchQuery query);

    /**
     * Whether each of these dishes can still be ordered (US-21).
     *
     * <p>Answers for every id asked about, including ids this restaurant has
     * never published, so a basket can be checked in one call.
     *
     * @throws be.kdg.keepdishesgoing.common.exceptions.NotFoundException
     *         if no such restaurant exists
     */
    List<DishAvailability> checkAvailability(UUID restaurantId, List<UUID> dishIds);
}
