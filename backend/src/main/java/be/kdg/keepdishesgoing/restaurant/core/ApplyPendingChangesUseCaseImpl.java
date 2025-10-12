package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ApplyPendingChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ApplyPendingChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ApplyPendingChangesUseCaseImpl implements ApplyPendingChangesUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public ApplyPendingChangesUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public void applyPendingChanges(ApplyPendingChangesCommand command) {
        Restaurant restaurant = restaurantRepositoryPort.findById(RestaurantId.of(command.restaurantId()))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + command.restaurantId()));

        restaurant.publishAllPendingChanges();
        restaurantRepositoryPort.saveRestaurant(restaurant);
    }
}
