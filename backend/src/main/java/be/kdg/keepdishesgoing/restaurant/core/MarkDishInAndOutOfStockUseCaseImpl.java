package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.MarkDishInAndOutOfStockUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishInStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishOutOfStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateDishPort;
import org.springframework.stereotype.Service;

@Service
public class MarkDishInAndOutOfStockUseCaseImpl implements MarkDishInAndOutOfStockUseCase {
    private final UpdateDishPort updateDishPort;
    private final LoadDishPort loadDishPort;
    private final PublishEventPort publishEventPort;

    public MarkDishInAndOutOfStockUseCaseImpl(UpdateDishPort updateDishPort, LoadDishPort loadDishPort, PublishEventPort publishEventPort) {
        this.updateDishPort = updateDishPort;
        this.loadDishPort = loadDishPort;

        this.publishEventPort = publishEventPort;
    }

    @Override
    public void markDishInStock(MarkDishInStockCommand command) {
        var dishId = DishId.of(command.dishId());

        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));
        try {
            dish.markDishInStock();
        } catch (Exception e) {
            throw new RuntimeException("Could not mark Dish in stock", e);
        }

        updateDishPort.updateDish(dish);
        publishEventPort.publishAll(dish.getUncommittedEvents());
        dish.markEventAsCommitted();
    }

    @Override
    public void markDishOutOfStock(MarkDishOutOfStockCommand command) {
        var dishId = DishId.of(command.dishId());

        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));
        try {
            dish.markDishOutOfStock();
        } catch (Exception e) {
            throw new RuntimeException("Could not mark Dish out of stock", e);
        }

        updateDishPort.updateDish(dish);

        publishEventPort.publishAll(dish.getUncommittedEvents());
        dish.markEventAsCommitted();

    }
}
