package be.kdg.keepdishesgoing.restaurant.port.in;

import java.util.UUID;

public record PublishDishCommand(
        UUID restaurantId,
        UUID dishId
) {
}
