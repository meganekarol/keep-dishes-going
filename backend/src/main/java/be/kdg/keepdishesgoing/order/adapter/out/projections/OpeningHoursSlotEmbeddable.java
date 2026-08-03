package be.kdg.keepdishesgoing.order.adapter.out.projections;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Persistent form of one opening window inside the restaurant projection.
 */
@Embeddable
public class OpeningHoursSlotEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    protected OpeningHoursSlotEmbeddable() {
        // JPA requires default constructor
    }

    public OpeningHoursSlotEmbeddable(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    /** Same exclusive comparison as {@code OpeningHoursSlot.covers}. */
    public boolean covers(LocalTime time) {
        return time.isAfter(openTime) && time.isBefore(closeTime);
    }
}
