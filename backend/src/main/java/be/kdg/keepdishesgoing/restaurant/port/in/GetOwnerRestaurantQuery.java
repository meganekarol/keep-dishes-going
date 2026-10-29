package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;

import java.util.Optional;
import java.util.UUID;

/**
 * The one restaurant belonging to an owner (US-31).
 */
public interface GetOwnerRestaurantQuery {

    /** @return empty while the owner has not created their restaurant yet */
    Optional<Restaurant> getOwnerRestaurant(UUID ownerId);
}
