package be.kdg.keepdishesgoing.restaurant.adapter.out.projections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * How many orders a restaurant still has to cook (US-19).
 *
 * <p>A projection the restaurant context keeps for itself, fed by order events.
 * The order context owns orders, and the catalogue needs a number from them to
 * quote a delivery time; reaching across for it would point this context at the
 * one that already points here.
 *
 * <p>Counts orders between being placed and being marked ready for pickup, which
 * is exactly the window in which they are occupying the kitchen.
 */
@Entity
@Table(name = "restaurant_busyness", schema = "kdg_restaurant")
public class RestaurantBusynessJpaEntity {

    @Id
    private UUID restaurantId;

    @Column(nullable = false)
    private int ordersNotReady;

    protected RestaurantBusynessJpaEntity() {
        // For JPA
    }

    public RestaurantBusynessJpaEntity(UUID restaurantId) {
        this.restaurantId = restaurantId;
        this.ordersNotReady = 0;
    }

    public void orderArrived() {
        this.ordersNotReady++;
    }

    /**
     * Floored at zero. A missed placement event would otherwise leave the
     * counter negative for good, and a negative queue is worse than a wrong one.
     */
    public void orderLeftTheKitchen() {
        this.ordersNotReady = Math.max(0, this.ordersNotReady - 1);
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public int getOrdersNotReady() {
        return ordersNotReady;
    }
}
