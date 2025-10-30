package be.kdg.keepdishesgoing.order.adapter.out.projections;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection (read model) of restaurants for the Order bounded context.
 * This is a denormalized view maintained by listening to Restaurant context events.
 * 
 * Used to check if a restaurant is open/closed when placing orders.
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
    
    @Column(nullable = false)
    private boolean isOpen;
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructors
    
    protected RestaurantProjectionJpaEntity() {
        // JPA requires default constructor
    }
    
    public RestaurantProjectionJpaEntity(UUID restaurantId, String restaurantName,
                                        String cuisineType, boolean isOpen) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.cuisineType = cuisineType;
        this.isOpen = isOpen;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public UUID getRestaurantId() {
        return restaurantId;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void open() {
        this.isOpen = true;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void close() {
        this.isOpen = false;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
}