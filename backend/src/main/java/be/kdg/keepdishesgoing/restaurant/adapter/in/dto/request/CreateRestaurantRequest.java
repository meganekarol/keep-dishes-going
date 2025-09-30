package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record CreateRestaurantRequest(
        String name,
        Address address,
        String contactEmail,
        String cuisineType,
        int defaultPreparationTime,
        Map<DayOfWeek, TimeRange> openingHours,
        List<String> pictureUrls
) {
}
