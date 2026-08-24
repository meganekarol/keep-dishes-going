package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;

import java.util.ArrayList;
import java.util.List;

/**
 * The dishes a restaurant currently has live.
 *
 * <p>Publishing goes through here rather than through a dish, because the cap of
 * ten live dishes (US-27) is a rule about the whole menu and one dish cannot see
 * the others.
 */
public class RestaurantMenu {

    private static final int MAX_PUBLISHED_DISHES = 10;

    private final RestaurantId restaurantId;
    private final List<DishId> publishedDishes = new ArrayList<>();

    /**
     * Creates an empty menu.
     *
     * @param restaurantId the restaurant the menu belongs to
     */
    public RestaurantMenu(RestaurantId restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Rebuilds a menu from what was stored.
     *
     * @param restaurantId    the restaurant the menu belongs to
     * @param publishedDishes the dishes that are already live
     */
    public RestaurantMenu(RestaurantId restaurantId, List<DishId> publishedDishes) {
        this.restaurantId = restaurantId;
        this.publishedDishes.addAll(publishedDishes);
    }

    /**
     * Creates an empty menu for a restaurant.
     *
     * @param restaurantId the restaurant the menu belongs to
     * @return a menu with nothing published yet
     */
    public static RestaurantMenu forRestaurant(RestaurantId restaurantId) {
        return new RestaurantMenu(restaurantId);
    }

    /**
     * Returns how many dishes a restaurant may have live at once.
     *
     * @return the cap set by US-27
     */
    public static int maxPublishedDishes() {
        return MAX_PUBLISHED_DISHES;
    }

    /**
     * Puts a dish on the live menu. Publishing one that is already live does nothing.
     *
     * @param dishId the dish to publish
     * @throws IllegalStateException if the menu is already full
     */
    public void publishDish(DishId dishId) {
        if (publishedDishes.size() >= MAX_PUBLISHED_DISHES && !publishedDishes.contains(dishId)) {
            throw new IllegalStateException("Cannot publish more than " + MAX_PUBLISHED_DISHES + " dishes");
        }
        if (!publishedDishes.contains(dishId)) {
            publishedDishes.add(dishId);
        }
    }

    /**
     * Takes a dish off the live menu.
     *
     * @param dishId the dish to unpublish
     */
    public void unpublishDish(DishId dishId) {
        publishedDishes.remove(dishId);
    }

    /**
     * Returns which restaurant this menu belongs to.
     *
     * @return the restaurant id
     */
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    /**
     * Lists the dishes that are live.
     *
     * @return the published dish ids
     */
    public List<DishId> getPublishedDishes() {
        return publishedDishes;
    }
}
