package be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Embeddable
public class OpeningHoursEmbeddable {

    @ElementCollection
    @CollectionTable(
            name = "restaurant_opening_hours",
            schema = "kdg_restaurant",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @MapKeyColumn(name = "day_of_week")
    private Map<String, TimeRangeEmbeddable> schedule = new HashMap<>();

    public OpeningHoursEmbeddable() {
    }

    public OpeningHoursEmbeddable(Map<String, TimeRangeEmbeddable> schedule) {
        this.schedule = schedule;
    }

    public Map<String, TimeRangeEmbeddable> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, TimeRangeEmbeddable> schedule) {
        this.schedule = schedule;
    }
}
