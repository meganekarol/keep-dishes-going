package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Set;

public record DishVersion(
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
