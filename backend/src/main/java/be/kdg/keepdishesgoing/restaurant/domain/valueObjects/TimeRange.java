package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.time.LocalTime;

/* By default, records automatically generate:
A constructor
A value() accessor method
equals(), hashCode(), and toString()*/
public record TimeRange(LocalTime open, LocalTime closed) {

    // Compact Constructor
    public TimeRange {
        if (closed.isBefore(open)) {
            throw new IllegalArgumentException("closed must be before open");
        }
    }

    public boolean isWithin(LocalTime time) {
        return time.isAfter(open) && time.isBefore(closed);
    }
}
