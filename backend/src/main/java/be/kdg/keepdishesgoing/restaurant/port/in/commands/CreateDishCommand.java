package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateDishCommand(
        UUID restaurantId,
        String name,
        DishType type,
        Set<FoodTag> foodTags,
        String description,
        BigDecimal price,
        String pictureUrl
) {
}
