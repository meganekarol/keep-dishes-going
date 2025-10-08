package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.MarkDishInAndOutOfStockUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.MarkDishInStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.MarkDishOutOfStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class MarkDishInAndOutOfStockUseCaseImpl implements MarkDishInAndOutOfStockUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public MarkDishInAndOutOfStockUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public void markDishInStock(MarkDishInStockCommand command) {
        Restaurant restaurant = restaurantRepositoryPort
                .findById(RestaurantId.of(command.restaurantId()))
                .orElseThrow( () -> new IllegalArgumentException("Restaurant not found"));
        var dish = DishId.of(command.dishId());
        try {
            restaurant.markDishInStock(dish);
        } catch (Exception e) {
            throw new RuntimeException("Could not mark Dish In stock", e);
        }

        restaurantRepositoryPort.saveRestaurant(restaurant);
    }

    @Override
    public void markDishOutOfStock(MarkDishOutOfStockCommand command) {
        Restaurant restaurant = restaurantRepositoryPort
                .findById(RestaurantId.of(command.restaurantId()))
                .orElseThrow( () -> new IllegalArgumentException("Restaurant not found"));
        var dish = DishId.of(command.dishId());
        try {
            restaurant.markDishOutOfStock(dish);
        } catch (Exception e) {
            throw new RuntimeException("Could not mark Dish out of stock", e);
        }

        restaurantRepositoryPort.saveRestaurant(restaurant);
    }
}
