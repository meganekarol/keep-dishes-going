package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;

public interface CreateRestaurantUseCase {
    Restaurant createRestaurant(CreateRestaurantCommand command);
}
