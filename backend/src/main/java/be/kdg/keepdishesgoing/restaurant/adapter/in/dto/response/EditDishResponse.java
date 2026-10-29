package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;

public record EditDishResponse(
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
