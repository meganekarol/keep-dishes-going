package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantStatus;

import java.util.UUID;

public record ChangeRestaurantStatusCommand(
        UUID restaurantId,
        RestaurantStatus newStatus
) {
}
