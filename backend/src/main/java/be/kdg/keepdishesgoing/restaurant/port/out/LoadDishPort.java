package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoadDishPort {
    Optional<Dish> loadById(DishId dish);
    List<Dish> loadAll();
    List<Dish> loadPendingDishesByRestaurant(RestaurantId restaurant);
    List<Dish> loadReadyForScheduledPublish(LocalDateTime time);
}
