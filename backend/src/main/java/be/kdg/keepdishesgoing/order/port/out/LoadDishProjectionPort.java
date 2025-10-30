package be.kdg.keepdishesgoing.order.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port for loading dish projections
 */
public interface LoadDishProjectionPort {
    
    /**
     * Check if a dish is available for ordering
     * 
     * @param dishId the dish ID
     * @return true if available, false if out of stock or unpublished
     * @throws IllegalArgumentException if dish not found
     */
    boolean isDishAvailable(UUID dishId);
    
    /**
     * Check if all dishes in a list are available
     * 
     * @param dishIds list of dish IDs
     * @return true if all are available
     */
    boolean areAllDishesAvailable(List<UUID> dishIds);
    
    /**
     * Get the current price of a dish
     * 
     * @param dishId the dish ID
     * @return the current price
     * @throws IllegalArgumentException if dish not found
     */
    BigDecimal getDishPrice(UUID dishId);
    
    /**
     * Get the name of a dish
     * 
     * @param dishId the dish ID
     * @return the dish name
     * @throws IllegalArgumentException if dish not found
     */
    String getDishName(UUID dishId);
    
    /**
     * Verify a dish belongs to a specific restaurant
     * 
     * @param dishId the dish ID
     * @param restaurantId the restaurant ID
     * @return true if the dish belongs to the restaurant
     */
    boolean dishBelongsToRestaurant(UUID dishId, UUID restaurantId);
}