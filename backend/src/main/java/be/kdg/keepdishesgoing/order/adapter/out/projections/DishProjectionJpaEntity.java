package be.kdg.keepdishesgoing.order.adapter.out.projections;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Projection (read model) of dishes for the Order bounded context.
 * This is a denormalized view maintained by listening to Restaurant context events.
 * 
 * The Order context doesn't own dishes - it maintains a projection for querying
 * available dishes when customers are building their basket.
 */
@Entity
@Table(name = "dish_projections", schema = "kdg_order")
public class DishProjectionJpaEntity {
    
    @Id
    private UUID dishId;
    
    @Column(nullable = false)
    private UUID restaurantId;
    
    @Column(nullable = false)
    private String dishName;
    
    @Column(nullable = false)
    private String dishType; // STARTER, MAIN, DESSERT
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishProjectionStatus status; // AVAILABLE, OUT_OF_STOCK, UNPUBLISHED
    
    @Column(nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructors
    
    protected DishProjectionJpaEntity() {
        // JPA requires default constructor
    }
    
    public DishProjectionJpaEntity(UUID dishId, UUID restaurantId, String dishName,
                                   String dishType, BigDecimal price, String description,
                                   DishProjectionStatus status) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;
        this.dishName = dishName;
        this.dishType = dishType;
        this.price = price;
        this.description = description;
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public UUID getDishId() {
        return dishId;
    }
    
    public UUID getRestaurantId() {
        return restaurantId;
    }
    
    public String getDishName() {
        return dishName;
    }
    
    public String getDishType() {
        return dishType;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public DishProjectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(DishProjectionStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void markAsUnpublished() {
        this.status = DishProjectionStatus.UNPUBLISHED;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void markAsOutOfStock() {
        this.status = DishProjectionStatus.OUT_OF_STOCK;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void markAsAvailable() {
        this.status = DishProjectionStatus.AVAILABLE;
        this.lastUpdated = LocalDateTime.now();
    }
}