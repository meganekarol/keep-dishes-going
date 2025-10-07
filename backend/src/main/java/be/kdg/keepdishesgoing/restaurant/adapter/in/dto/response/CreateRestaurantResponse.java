package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateRestaurantResponse(
        UUID restaurantId,
        String name,
        Address address,
        String contactEmail,
        String cuisineType,
        int defaultPreparationTime,
        Map<DayOfWeek, TimeRange> openingHours,
        List<String> pictureUrls
) {
}
