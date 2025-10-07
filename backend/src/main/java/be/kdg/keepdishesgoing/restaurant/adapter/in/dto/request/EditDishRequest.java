package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;

public record EditDishRequest(
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
