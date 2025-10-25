package be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;

import java.time.DayOfWeek;
import java.util.Map;

public record UpdateRestaurantHours(
        Map<DayOfWeek, TimeRange> schedule
) {
}
