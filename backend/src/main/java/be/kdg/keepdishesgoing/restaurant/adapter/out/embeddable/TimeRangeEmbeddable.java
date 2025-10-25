package be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;

@Embeddable
public class TimeRangeEmbeddable {
    private LocalTime openTime;
    private LocalTime closeTime;

    public TimeRangeEmbeddable() {
    }

    public TimeRangeEmbeddable(LocalTime openTime, LocalTime closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}

