package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ApplyPendingChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ApplyPendingChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ApplyPendingChangesUseCaseImpl implements ApplyPendingChangesUseCase {

    private final LoadDishPort loadDishPort;
    private final UpdateDishPort updateDishPort;
    private final LoadRestaurantPort loadRestaurantPort;
    private final UpdateRestaurantPort updateRestaurantPort;

    public ApplyPendingChangesUseCaseImpl(
            LoadDishPort loadDishPort,
            UpdateDishPort updateDishPort,
            LoadRestaurantPort loadRestaurantPort,
            UpdateRestaurantPort updateRestaurantPort) {
        this.loadDishPort = loadDishPort;
        this.updateDishPort = updateDishPort;
        this.loadRestaurantPort = loadRestaurantPort;
        this.updateRestaurantPort = updateRestaurantPort;
    }

    @Override
    public void applyPendingChanges(ApplyPendingChangesCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());

        // Load aggregates
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        List<Dish> pendingDishes = loadDishPort.loadPendingDishesByRestaurant(restaurantId);
        if (pendingDishes.isEmpty()) {
            return;
        }

        // Publish each pending dish and enforce menu policy
        for (Dish dish : pendingDishes) {
            restaurant.getMenu().publishDish(dish.getDishId()); // enforce "max 10"
            dish.publishDish();
            updateDishPort.updateDish(dish);
        }

        // Persist the updated restaurant (menu state updated)
        updateRestaurantPort.updateRestaurant(restaurant);
    }
}
