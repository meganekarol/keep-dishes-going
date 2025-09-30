package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateRestaurantUseCaseImpl implements CreateRestaurantUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public CreateRestaurantUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public Restaurant createRestaurant(CreateRestaurantCommand command) {

        OwnerId ownerId = OwnerId.of(command.ownerId()); // Later get the id from authentication

        Optional<Restaurant> existingRestaurant =
                restaurantRepositoryPort.findByOwnerId(ownerId);

        if (existingRestaurant.isPresent()) {
            throw new RuntimeException("Restaurant with id " + ownerId + " already exists");
        }

        Restaurant restaurant = Restaurant.createRestaurant(
                RestaurantId.create(),
                ownerId,
                command.name(),
                command.address(),
                command.contactEmail(),
                command.cuisineType(),
                command.defaultPreparationTime(),
                command.openingHours(),
                command.pictureUrls()
        );

        return restaurantRepositoryPort.saveRestaurant(restaurant);

    }
}
