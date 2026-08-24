package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.LocalTime;

/**
 * One opening window on a day.
 *
 * @param open   the time the restaurant opens
 * @param closed the time it closes, which must be after it opens
 */
public record TimeRange(LocalTime open, LocalTime closed) {

    /**
     * Checks the window makes sense.
     *
     * @throws IllegalArgumentException if the closing time is before the opening time
     */
    public TimeRange {
        if (closed.isBefore(open)) {
            throw new IllegalArgumentException("closed must be before open");
        }
    }

    /**
     * Checks whether a time falls inside this window.
     *
     * @param time the time to check
     * @return {@code true} if the time is between opening and closing
     */
    public boolean isWithin(LocalTime time) {
        return time.isAfter(open) && time.isBefore(closed);
    }
}
