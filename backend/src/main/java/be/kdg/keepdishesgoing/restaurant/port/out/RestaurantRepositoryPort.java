package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.util.Optional;

public interface RestaurantRepositoryPort {
    Restaurant saveRestaurant(Restaurant restaurant);
    Optional<Restaurant> findByOwnerId(OwnerId ownerId);
    Optional<Restaurant> findById(RestaurantId restaurantId);
}
