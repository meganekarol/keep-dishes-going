package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.util.Optional;

public interface LoadRestaurantMenuPort {
    Optional<RestaurantMenu> loadByRestaurantId(RestaurantId restaurantId);
}
