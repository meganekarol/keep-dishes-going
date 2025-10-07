package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RestaurantRepositoryInMemoryAdapter implements RestaurantRepositoryPort {

    private final Map<RestaurantId, Restaurant> restaurants = new HashMap<>();

    @Override
    public Restaurant saveRestaurant(Restaurant restaurant) {
       this.restaurants.put(restaurant.getRestaurantId(), restaurant);
       return restaurant;
    }

    @Override
    public Optional<Restaurant> findByOwnerId(OwnerId ownerId) {
        return restaurants.values().stream()
                .filter(restaurant -> restaurant.getOwnerId().equals(ownerId)).findFirst();
    }

    @Override
    public Optional<Restaurant> findById(RestaurantId restaurantId) {
        return Optional.of(restaurants.get(restaurantId));
    }
}
