package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.GetOwnerRestaurantCommand;

import java.util.Optional;

public interface GetOwnerRestaurant {
    Optional<Restaurant> getOwnerRestaurant(GetOwnerRestaurantCommand command);
}
