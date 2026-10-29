package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.EditDishAsDraftCommand;

/** Lets an owner change a dish without touching the live menu (US-03). */
public interface EditDishAsDraftUseCase {
    /**
     * Saves the new details as a draft.
     *
     * @param command which dish to edit and what it should say
     * @return the dish with its updated draft
     */
    Dish editDish(EditDishAsDraftCommand command);
}
