package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.PublishAndUnpublishDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.UnpublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.*;
import org.springframework.stereotype.Service;

@Service
public class PublishAndUnpublishDishUseCaseImpl implements PublishAndUnpublishDishUseCase {

    private final LoadDishPort loadDishPort;
    private final UpdateDishPort updateDishPort;
    private final LoadRestaurantPort loadRestaurantPort;
    private final UpdateRestaurantPort updateRestaurantPort;
    private final PublishEventPort publishEventPort;

    public PublishAndUnpublishDishUseCaseImpl(LoadDishPort loadDishPort, UpdateDishPort updateDishPort, LoadRestaurantPort loadRestaurantPort, UpdateRestaurantPort updateRestaurantPort, PublishEventPort publishEventPort) {
        this.loadDishPort = loadDishPort;
        this.updateDishPort = updateDishPort;
        this.loadRestaurantPort = loadRestaurantPort;
        this.updateRestaurantPort = updateRestaurantPort;
        this.publishEventPort = publishEventPort;
    }


    @Override
    public void publishDish(PublishDishCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        var dishId = DishId.of(command.dishId());

        // Load aggregates
        var restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        var dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        // Verify ownership
        if (!dish.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("Dish does not belong to this restaurant");
        }

        // Enforce policy via the restaurant (menu)
        restaurant.getMenu().publishDish(dish.getDishId());

        // Domain behavior inside Dish (raises DishPublishedEvent)
        dish.publishDish();

        // Persist updates
        updateDishPort.updateDish(dish);
        updateRestaurantPort.updateRestaurant(restaurant);

        // Publish events
        publishEventPort.publishAll(dish.getUncommittedEvents());
        publishEventPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        dish.markEventAsCommitted();
        restaurant.markEventAsCommitted();
    }


    @Override
    public void unpublishDish(UnpublishDishCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        var dishId = DishId.of(command.dishId());

        // Load aggregates
        var restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        var dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        // Verify ownership
        if (!dish.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("Dish does not belong to this restaurant");
        }

        // Enforce policy via the restaurant (menu)
        restaurant.getMenu().unpublishDish(dish.getDishId());

        // Domain behavior inside Dish (raises DishPublishedEvent)
        dish.unpublishDish();

        // Persist updates
        updateDishPort.updateDish(dish);
        updateRestaurantPort.updateRestaurant(restaurant);

        publishEventPort.publishAll(dish.getUncommittedEvents());
        publishEventPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        dish.markEventAsCommitted();
        restaurant.markEventAsCommitted();

    }
}