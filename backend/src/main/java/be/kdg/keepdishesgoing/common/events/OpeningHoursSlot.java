package be.kdg.keepdishesgoing.common.events;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One day's opening window, as it travels between bounded contexts.
 *
 * <p>Part of the shared kernel, so it deliberately carries plain JDK types only:
 * the Order context must be able to reason about opening hours without depending
 * on the Restaurant context's {@code OpeningHours} value object.
 */
public record OpeningHoursSlot(
        DayOfWeek dayOfWeek,
        LocalTime openTime,
        LocalTime closeTime
) {

    public OpeningHoursSlot {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week cannot be null");
        }
        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("Opening and closing time cannot be null");
        }
    }

    /**
     * Mirrors {@code TimeRange.isWithin} in the Restaurant context. Both contexts
     * must answer "are you open?" identically, so the comparison is intentionally
     * kept exclusive at both ends here rather than "improved" on one side only.
     */
    public boolean covers(LocalTime time) {
        return time.isAfter(openTime) && time.isBefore(closeTime);
    }
}
