package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;

public record CreateDishRequest(
        String name,
        DishType type,
        Set<String> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
