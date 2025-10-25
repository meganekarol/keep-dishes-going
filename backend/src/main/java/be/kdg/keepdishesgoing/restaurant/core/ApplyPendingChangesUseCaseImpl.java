package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
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
    private final LoadRestaurantMenuPort loadRestaurantMenuPort;
    private final UpdateRestaurantMenuPort updateRestaurantMenuPort;

    public ApplyPendingChangesUseCaseImpl(LoadDishPort loadDishPort, UpdateDishPort updateDishPort, LoadRestaurantMenuPort loadRestaurantMenuPort, UpdateRestaurantMenuPort updateRestaurantMenuPort) {
        this.loadDishPort = loadDishPort;
        this.updateDishPort = updateDishPort;
        this.loadRestaurantMenuPort = loadRestaurantMenuPort;
        this.updateRestaurantMenuPort = updateRestaurantMenuPort;
    }

    @Override
    public void applyPendingChanges(ApplyPendingChangesCommand command) {

        var restaurantId = RestaurantId.of(command.restaurantId());

        // Load or create the RestaurantMenu (policy aggregate)
        RestaurantMenu menu = loadRestaurantMenuPort.loadByRestaurantId(restaurantId)
                .orElseGet(() -> RestaurantMenu.forRestaurant(restaurantId));

        // Load all dishes with pending changes
        List<Dish> pendingDishes = loadDishPort.loadPendingDishesByRestaurant(restaurantId);

        if (pendingDishes.isEmpty()) {
            return;
        }

        // Publish each pending dish while enforcing menu policy
        for (Dish dish : pendingDishes) {
            // enforce "max 10" via RestaurantMenu
            menu.publishDish(dish.getDishId());
            dish.publishDish();
            updateDishPort.updateDish(dish);
        }

        // Persist updated RestaurantMenu (now containing new published dishes)
        updateRestaurantMenuPort.updateRestaurantMenu(menu);
    }
}
