package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
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
    private final LoadRestaurantMenuPort loadRestaurantMenuPort;
    private final UpdateRestaurantMenuPort updateRestaurantMenuPort;

    public PublishAndUnpublishDishUseCaseImpl(LoadDishPort loadDishPort, UpdateDishPort updateDishPort, LoadRestaurantMenuPort loadRestaurantMenuPort, UpdateRestaurantMenuPort updateRestaurantMenuPort) {
        this.loadDishPort = loadDishPort;
        this.updateDishPort = updateDishPort;
        this.loadRestaurantMenuPort = loadRestaurantMenuPort;
        this.updateRestaurantMenuPort = updateRestaurantMenuPort;
    }


    @Override
    public void publishDish(PublishDishCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        var dishId = DishId.of(command.dishId());


        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        if (!dish.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("Dish does not belong to this restaurant");
        }

        // Load or initialize RestaurantMenu
        RestaurantMenu menu = loadRestaurantMenuPort.loadByRestaurantId(restaurantId)
                .orElseGet(() -> RestaurantMenu.forRestaurant(restaurantId));

        // Enforce policy
        menu.publishDish(dishId);

        // Update aggregates
        dish.publishDish();

        // Persist both
        updateRestaurantMenuPort.updateRestaurantMenu(menu);
        updateDishPort.updateDish(dish);
    }

    @Override
    public void unpublishDish(UnpublishDishCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        var dishId = DishId.of(command.dishId());


        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        if (!dish.getRestaurantId().equals(restaurantId)) {
            throw new IllegalStateException("Dish does not belong to this restaurant");
        }

        // Load or initialize RestaurantMenu
        RestaurantMenu menu = loadRestaurantMenuPort.loadByRestaurantId(restaurantId)
                .orElseGet(() -> RestaurantMenu.forRestaurant(restaurantId));

        menu.unpublishDish(dishId);
        dish.unpublishDish();

        updateRestaurantMenuPort.updateRestaurantMenu(menu);
        updateDishPort.updateDish(dish);
    }
}