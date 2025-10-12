package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishInStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishOutOfStockCommand;

public interface MarkDishInAndOutOfStockUseCase {
    void markDishInStock(MarkDishInStockCommand command);
    void markDishOutOfStock(MarkDishOutOfStockCommand command);
}
