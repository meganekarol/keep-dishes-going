package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateDishCommand;

/** Lets an owner add a dish to their menu (US-03). */
public interface CreateDishUseCase {
    /**
     * Creates a dish as a draft, so customers do not see it yet.
     *
     * @param command the details the owner typed in
     * @return the new dish
     */
    Dish createDish(CreateDishCommand command);
}
