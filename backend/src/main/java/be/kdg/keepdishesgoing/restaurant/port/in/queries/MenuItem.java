package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishVersion;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

/**
 * One dish as a customer sees it: the published version only.
 *
 * <p>Draft content never appears here. An owner editing a dish is working on a
 * draft that nobody has approved for the menu yet, so exposing it would show
 * customers prices they cannot order at.
 *
 * @param available {@code false} means the dish is on the menu but temporarily
 *                  sold out. It is still shown, greyed out, which is the whole
 *                  point of out-of-stock being a different state from unpublished.
 */
public record MenuItem(
        UUID dishId,
        UUID restaurantId,
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl,
        boolean available
) {

    /**
     * @param dish must satisfy {@link Dish#isOnMenu()}; dishes that never reached
     *             the menu have no published version to read from.
     */
    public static MenuItem from(Dish dish) {
        DishVersion published = dish.getPublishedVersion();
        return new MenuItem(
                dish.getDishId().uuid(),
                dish.getRestaurantId().uuid(),
                published.name(),
                published.type(),
                published.foodTags(),
                published.description(),
                published.price(),
                published.pictureUrl(),
                dish.isPublished()
        );
    }
}
