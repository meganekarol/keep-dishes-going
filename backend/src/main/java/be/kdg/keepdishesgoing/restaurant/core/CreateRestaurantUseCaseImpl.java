package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CreateRestaurantUseCaseImpl implements CreateRestaurantUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final PublishEventPort publishEventPort;

    public CreateRestaurantUseCaseImpl(LoadRestaurantPort loadRestaurantPort, SaveRestaurantPort saveRestaurantPort, PublishEventPort publishEventPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public Restaurant createRestaurant(CreateRestaurantCommand command) {

       var ownerId = OwnerId.of(command.ownerId()); // comes from Keycloak-authenticated user

        Optional<Restaurant> existingRestaurant =
                loadRestaurantPort.loadByOwnerId(ownerId);

        if (existingRestaurant.isPresent()) {
            // One restaurant per owner. A conflict, not a server fault — a bare
            // RuntimeException here surfaced as a 500 and read like a crash.
            throw new IllegalStateException("This owner already has a restaurant");
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

        saveRestaurantPort.saveRestaurant(restaurant);

        publishEventPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();

        return restaurant;

    }
}
