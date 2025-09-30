package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.DayOfWeek;
import java.util.Map;

public record OpeningHours(Map<DayOfWeek, TimeRange> schedule) {
}
