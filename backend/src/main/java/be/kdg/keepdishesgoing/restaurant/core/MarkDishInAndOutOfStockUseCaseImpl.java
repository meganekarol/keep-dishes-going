package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.port.in.MarkDishInAndOutOfStockUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishInStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishOutOfStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveDishPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MarkDishInAndOutOfStockUseCaseImpl implements MarkDishInAndOutOfStockUseCase {
    private final SaveDishPort saveDishPort;
    private final LoadDishPort loadDishPort;
    private final PublishEventPort publishEventPort;

    public MarkDishInAndOutOfStockUseCaseImpl(SaveDishPort saveDishPort, LoadDishPort loadDishPort, PublishEventPort publishEventPort) {
        this.saveDishPort = saveDishPort;
        this.loadDishPort = loadDishPort;

        this.publishEventPort = publishEventPort;
    }

    @Override
    public void markDishInStock(MarkDishInStockCommand command) {
        var dishId = DishId.of(command.dishId());

        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish", command.dishId()));

        // Let the domain exception through: wrapping it in a RuntimeException
        // hid the reason and made every rule violation look like a server fault.
        dish.markDishInStock();

        saveDishPort.saveDish(dish);
        publishEventPort.publishAll(dish.getUncommittedEvents());
        dish.markEventAsCommitted();
    }

    @Override
    public void markDishOutOfStock(MarkDishOutOfStockCommand command) {
        var dishId = DishId.of(command.dishId());

        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish", command.dishId()));

        dish.markDishOutOfStock();

        saveDishPort.saveDish(dish);

        publishEventPort.publishAll(dish.getUncommittedEvents());
        dish.markEventAsCommitted();

    }
}
