package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.util.ArrayList;
import java.util.List;

public class RestaurantMenu {

    private static final int MAX_PUBLISHED_DISHES = 10;

    private final RestaurantId restaurantId;
    private final List<DishId> publishedDishes = new ArrayList<>();

    public RestaurantMenu(RestaurantId restaurantId) {
        this.restaurantId = restaurantId;
    }

    // Factory method
    public static RestaurantMenu forRestaurant(RestaurantId restaurantId) {
        return new RestaurantMenu(restaurantId);
    }

    // Restore from persistence
    public RestaurantMenu(RestaurantId restaurantId, List<DishId> publishedDishes) {
        this.restaurantId = restaurantId;
        this.publishedDishes.addAll(publishedDishes);
    }

    // Domain behavior
    public void publishDish(DishId dishId) {
        if (publishedDishes.size() >= MAX_PUBLISHED_DISHES && !publishedDishes.contains(dishId)) {
            throw new IllegalStateException("Cannot publish more than " + MAX_PUBLISHED_DISHES + " dishes");
        }
        if (!publishedDishes.contains(dishId)) {
            publishedDishes.add(dishId);
        }
    }

    public void unpublishDish(DishId dishId) {
        publishedDishes.remove(dishId);
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public List<DishId> getPublishedDishes() {
        return publishedDishes;
    }
}
