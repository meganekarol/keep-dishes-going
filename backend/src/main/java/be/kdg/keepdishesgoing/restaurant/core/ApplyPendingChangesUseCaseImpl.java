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
    private final SaveDishPort saveDishPort;
    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final PublishEventPort publishEventPort;

    public ApplyPendingChangesUseCaseImpl(
            LoadDishPort loadDishPort,
            SaveDishPort saveDishPort,
            LoadRestaurantPort loadRestaurantPort,
            SaveRestaurantPort saveRestaurantPort,
            PublishEventPort publishEventPort) {
        this.loadDishPort = loadDishPort;
        this.saveDishPort = saveDishPort;
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void applyPendingChanges(ApplyPendingChangesCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());

        // Load aggregates
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        List<Dish> pendingDishes = loadDishPort.loadPendingDishesByRestaurant(restaurantId);
        if (pendingDishes.isEmpty()) {
            // Returning quietly here answered 200 to an owner whose changes had
            // in fact not gone live, usually because another tab published them.
            throw new IllegalStateException("There are no changes waiting to be published");
        }

        // Publish each pending dish and enforce menu policy
        for (Dish dish : pendingDishes) {
            restaurant.getMenu().publishDish(dish.getDishId()); // enforce "max 10"
            dish.publishDish();
            saveDishPort.saveDish(dish);

            // Without this the order module never hears about the dish, so
            // customers cannot order what the owner just put on the menu.
            publishEventPort.publishAll(dish.getUncommittedEvents());
            dish.markEventAsCommitted();
        }

        // Persist the updated restaurant (menu state updated)
        saveRestaurantPort.saveRestaurant(restaurant);
        publishEventPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();
    }
}
