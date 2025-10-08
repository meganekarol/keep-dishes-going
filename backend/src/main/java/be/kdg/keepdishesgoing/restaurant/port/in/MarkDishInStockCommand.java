package be.kdg.keepdishesgoing.restaurant.port.in;

import java.util.UUID;

public record MarkDishInStockCommand(
        UUID restaurantId,
        UUID dishId
) {
}
