package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantStatus;

public record UpdateRestaurantStatusRequest(
        RestaurantStatus status
) {
}
