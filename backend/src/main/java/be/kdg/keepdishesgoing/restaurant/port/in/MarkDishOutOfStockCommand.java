package be.kdg.keepdishesgoing.restaurant.port.in;

import java.util.UUID;

public record MarkDishOutOfStockCommand(
        UUID restaurantId,
        UUID dishId
) {
}
