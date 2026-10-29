package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import java.util.UUID;

public record PublishDishCommand(
        UUID restaurantId,
        UUID dishId
) {
}
