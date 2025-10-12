package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.EditDishAsDraftCommand;

public interface EditDishAsDraftUseCase {
    Dish editDish(EditDishAsDraftCommand command);
}
