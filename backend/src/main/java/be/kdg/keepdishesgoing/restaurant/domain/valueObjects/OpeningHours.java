package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

/**
 * When a restaurant is open, one window per day (US-09).
 *
 * <p>A day that is missing from the schedule is a day the restaurant is closed.
 *
 * @param schedule the opening window for each day the restaurant opens
 */
public record OpeningHours(Map<DayOfWeek, TimeRange> schedule) {

    /**
     * Copies the schedule so it cannot be changed afterwards.
     *
     * @throws IllegalArgumentException if the schedule is missing or empty
     */
    public OpeningHours {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("schedule cannot be null or empty");
        }
        schedule = Map.copyOf(schedule);
    }

    /**
     * Checks whether the restaurant is open at a given time on a given day.
     *
     * @param time the time of day to check
     * @param day  the day of the week to check
     * @return {@code true} if that day has a window and the time falls inside it
     */
    public boolean isOpenAt(LocalTime time, DayOfWeek day) {
        TimeRange range = schedule.get(day);
        return range != null && range.isWithin(time);
    }
}
