package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;

import java.util.Optional;

public interface UpdateRestaurantMenuPort {
    RestaurantMenu updateRestaurantMenu(RestaurantMenu restaurantMenu);
}
