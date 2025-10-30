package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;

public interface SaveRestaurantPort {
   Restaurant saveRestaurant(Restaurant restaurant);
}
