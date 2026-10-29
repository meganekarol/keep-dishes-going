package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;

public interface SaveDishPort {
    Dish saveDish(Dish dish);
}
