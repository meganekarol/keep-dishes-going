package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

public record OpeningHours(Map<DayOfWeek, TimeRange> schedule) {
    public boolean isOpenAt(LocalTime time, DayOfWeek day) {
        TimeRange range = schedule.get(day);
        return range != null && range.isWithin(time);
    }
}
