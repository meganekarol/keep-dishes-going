package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.util.List;
import java.util.Optional;

public interface LoadRestaurantPort {
    Optional<Restaurant> loadByOwnerId(OwnerId owner);
    Optional<Restaurant> loadById(RestaurantId restaurant);
    List<Restaurant> loadAll();
}
