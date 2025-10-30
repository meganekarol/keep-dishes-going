package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CreateRestaurantUseCaseImpl implements CreateRestaurantUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final UpdateRestaurantPort updateRestaurantPort;
    private final PublishEventPort publishEventPort;

    public CreateRestaurantUseCaseImpl(LoadRestaurantPort loadRestaurantPort, UpdateRestaurantPort updateRestaurantPort, PublishEventPort publishEventPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.updateRestaurantPort = updateRestaurantPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public Restaurant createRestaurant(CreateRestaurantCommand command) {

       var ownerId = OwnerId.of(command.ownerId()); // comes from Keycloak-authenticated user

        Optional<Restaurant> existingRestaurant =
                loadRestaurantPort.loadByOwnerId(ownerId);

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

        updateRestaurantPort.updateRestaurant(restaurant);

        publishEventPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();

        return restaurant;

    }
}
