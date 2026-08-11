package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.common.events.OpeningHoursSlot;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Projection (read model) of restaurants for the Order bounded context.
 * This is a denormalized view maintained by listening to Restaurant context events.
 *
 * <p>Openness is <em>derived</em>, never stored: the projection keeps the weekly
 * schedule plus any manual override, and evaluates them on demand. A stored
 * boolean would only ever change when somebody toggled it by hand, so a
 * restaurant open on its normal schedule would look closed to customers.
 */
@Entity
@Table(name = "restaurant_projections", schema = "kdg_order")
public class RestaurantProjectionJpaEntity {

    @Id
    private UUID restaurantId;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String cuisineType;

    @Enumerated(EnumType.STRING)
    @Column(name = "manual_override", nullable = false)
    private ManualOverride manualOverride = ManualOverride.NONE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "restaurant_projection_opening_hours",
            schema = "kdg_order",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<OpeningHoursSlotEmbeddable> openingHours = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    protected RestaurantProjectionJpaEntity() {
        // JPA requires default constructor
    }

    public RestaurantProjectionJpaEntity(UUID restaurantId, String restaurantName,
                                         String cuisineType, List<OpeningHoursSlot> slots) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.cuisineType = cuisineType;
        if (slots != null) {
            slots.stream()
                    .map(slot -> new OpeningHoursSlotEmbeddable(
                            slot.dayOfWeek(), slot.openTime(), slot.closeTime()))
                    .forEach(this.openingHours::add);
        }
        touch();
    }

    /**
     * Answers "is this restaurant taking orders at {@code when}?".
     *
     * <p>Deliberately the same rule as {@code Restaurant.isOpen} in the Restaurant
     * context: a manual override wins, otherwise the schedule decides. Called with
     * the current time at checkout, so a toggle takes effect on the next order.
     */
    public boolean isOpenAt(LocalDateTime when) {
        if (manualOverride == ManualOverride.FORCED_CLOSED) {
            return false;
        }
        if (manualOverride == ManualOverride.FORCED_OPEN) {
            return true;
        }
        return openingHours.stream()
                .filter(slot -> slot.getDayOfWeek() == when.getDayOfWeek())
                .anyMatch(slot -> slot.covers(when.toLocalTime()));
    }

    public void forceOpen() {
        this.manualOverride = ManualOverride.FORCED_OPEN;
        touch();
    }

    public void forceClosed() {
        this.manualOverride = ManualOverride.FORCED_CLOSED;
        touch();
    }

    /** The owner gave up their override, so the schedule decides again. */
    public void followSchedule() {
        this.manualOverride = ManualOverride.NONE;
        touch();
    }

    private void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public ManualOverride getManualOverride() {
        return manualOverride;
    }

    public List<OpeningHoursSlotEmbeddable> getOpeningHours() {
        return List.copyOf(openingHours);
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}
