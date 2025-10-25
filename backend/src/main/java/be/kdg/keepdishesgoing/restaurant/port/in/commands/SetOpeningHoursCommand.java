package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

public record SetOpeningHoursCommand(
        UUID restaurantId,
        Map<DayOfWeek, TimeRange> schedule) {
}
