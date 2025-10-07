package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;

public interface EditDishAsDraftUseCase {
    Dish editDish(EditDishAsDraftCommand command);
}
