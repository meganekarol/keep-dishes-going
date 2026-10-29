package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.GeocodeAddressPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantEventPublisherPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CreateRestaurantUseCaseImpl implements CreateRestaurantUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final RestaurantEventPublisherPort restaurantEventPublisherPort;
    private final GeocodeAddressPort geocodeAddressPort;

    public CreateRestaurantUseCaseImpl(LoadRestaurantPort loadRestaurantPort, SaveRestaurantPort saveRestaurantPort,
                                       RestaurantEventPublisherPort restaurantEventPublisherPort, GeocodeAddressPort geocodeAddressPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
        this.restaurantEventPublisherPort = restaurantEventPublisherPort;
        this.geocodeAddressPort = geocodeAddressPort;
    }

    @Override
    public Restaurant createRestaurant(CreateRestaurantCommand command) {

       var ownerId = OwnerId.of(command.ownerId()); // comes from Keycloak-authenticated user

        Optional<Restaurant> existingRestaurant =
                loadRestaurantPort.loadByOwnerId(ownerId);

        if (existingRestaurant.isPresent()) {
            // One restaurant per owner. A conflict, not a server fault. A bare
            // RuntimeException here surfaced as a 500 and read like a crash.
            throw new IllegalStateException("This owner already has a restaurant");
        }

        // Looked up once, here, rather than on every browse: an address does
        // not move, and Nominatim asks callers to be sparing.
        Address address = geocodeAddressPort.locate(command.address())
                .map(command.address()::locatedAt)
                .orElse(command.address());

        Restaurant restaurant = Restaurant.createRestaurant(
                RestaurantId.create(),
                ownerId,
                command.name(),
                address,
                command.contactEmail(),
                command.cuisineType(),
                command.defaultPreparationTime(),
                command.openingHours(),
                command.pictureUrls()
        );

        saveRestaurantPort.saveRestaurant(restaurant);

        restaurantEventPublisherPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();

        return restaurant;

    }
}
