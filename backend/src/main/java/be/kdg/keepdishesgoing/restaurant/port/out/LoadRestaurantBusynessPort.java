package be.kdg.keepdishesgoing.restaurant.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * How many orders each restaurant still has to cook, for the delivery
 * guesstimate (US-19).
 */
public interface LoadRestaurantBusynessPort {

    int ordersNotReadyFor(UUID restaurantId);

    /**
     * The whole catalogue in one query. Browsing quotes an estimate per
     * restaurant, and asking one at a time would be a query each.
     *
     * @return restaurants with nothing in the kitchen may be absent, so read a
     *         missing entry as zero
     */
    Map<UUID, Integer> ordersNotReadyByRestaurant();
}
