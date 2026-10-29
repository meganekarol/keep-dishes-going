package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateDishResponse(
        UUID restaurantId,
        UUID dishId,
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
