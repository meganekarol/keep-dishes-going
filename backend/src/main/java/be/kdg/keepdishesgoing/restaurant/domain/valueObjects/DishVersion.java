package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Set;

/**
 * The details of a dish at one point in its life.
 *
 * <p>A dish keeps two of these: the version customers can see, and the draft the
 * owner is still working on (US-03).
 *
 * @param name        the name of the dish
 * @param type        which course it belongs to
 * @param foodTags    the dietary labels on it
 * @param description what the dish is
 * @param price       what it costs
 * @param pictureUrl  a picture of it, or {@code null} if there is none
 */
public record DishVersion(
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
