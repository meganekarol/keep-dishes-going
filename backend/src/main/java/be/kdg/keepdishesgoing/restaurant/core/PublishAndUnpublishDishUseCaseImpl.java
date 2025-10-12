package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.PublishAndUnpublishDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.UnpublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PublishAndUnpublishDishUseCaseImpl implements PublishAndUnpublishDishUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public PublishAndUnpublishDishUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public void publishDish(PublishDishCommand command) {
        RestaurantId restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = restaurantRepositoryPort.findById(restaurantId).
                orElseThrow(() -> new RuntimeException("Restaurant not found"));

        DishId dish = DishId.of(command.dishId());

        try {
            restaurant.publishDish(dish);
        }catch (Exception e) {
            throw new RuntimeException("Error publishing dish", e);
        }

        restaurantRepositoryPort.saveRestaurant(restaurant);

    }

    @Override
    public void unpublishDish(UnpublishDishCommand command) {
        RestaurantId restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = restaurantRepositoryPort.findById(restaurantId).
                orElseThrow(() -> new RuntimeException("Restaurant not found"));

        DishId dish = DishId.of(command.dishId());

        try {
            restaurant.unpublishDish(dish);
        }catch (Exception e) {
            throw new RuntimeException("Error unpublishing dish", e);
        }

        restaurantRepositoryPort.saveRestaurant(restaurant);
    }
}
