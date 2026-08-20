package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.PublishAndUnpublishDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.UnpublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PublishAndUnpublishDishUseCaseImpl implements PublishAndUnpublishDishUseCase {

    private final LoadDishPort loadDishPort;
    private final SaveDishPort saveDishPort;
    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final RestaurantEventPublisherPort restaurantEventPublisherPort;

    public PublishAndUnpublishDishUseCaseImpl(LoadDishPort loadDishPort, SaveDishPort saveDishPort, LoadRestaurantPort loadRestaurantPort, SaveRestaurantPort saveRestaurantPort, RestaurantEventPublisherPort restaurantEventPublisherPort) {
        this.loadDishPort = loadDishPort;
        this.saveDishPort = saveDishPort;
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
        this.restaurantEventPublisherPort = restaurantEventPublisherPort;
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
        saveDishPort.saveDish(dish);
        saveRestaurantPort.saveRestaurant(restaurant);

        // Publish events
        restaurantEventPublisherPort.publishAll(dish.getUncommittedEvents());
        restaurantEventPublisherPort.publishAll(restaurant.getUncommittedEvents());

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
        saveDishPort.saveDish(dish);
        saveRestaurantPort.saveRestaurant(restaurant);

        restaurantEventPublisherPort.publishAll(dish.getUncommittedEvents());
        restaurantEventPublisherPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        dish.markEventAsCommitted();
        restaurant.markEventAsCommitted();

    }
}