package be.kdg.keepdishesgoing.restaurant.port.in;

import java.util.UUID;

public record UnpublishDishCommand(
        UUID restaurantId,
        UUID dishId
) {
}
