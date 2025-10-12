package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateDishCommand;

public interface CreateDishUseCase {
    Dish createDish(CreateDishCommand command);
}
