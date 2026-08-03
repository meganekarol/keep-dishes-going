package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

public record OpeningHours(Map<DayOfWeek, TimeRange> schedule) {
    public OpeningHours {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("schedule cannot be null or empty");
        }
        schedule = Map.copyOf(schedule);
    }
    public boolean isOpenAt(LocalTime time, DayOfWeek day) {
        TimeRange range = schedule.get(day);
        return range != null && range.isWithin(time);
    }
}
