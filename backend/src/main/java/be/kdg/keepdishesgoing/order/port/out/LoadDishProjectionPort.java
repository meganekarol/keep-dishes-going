package be.kdg.keepdishesgoing.order.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port for loading dish projections
 */
public interface LoadDishProjectionPort {
    
    /**
     * Name the dishes in a list that cannot be ordered
     *
     * <p>Names rather than a yes/no, so a refused order can say which dish
     * caused it (US-21).
     *
     * @param dishIds list of dish IDs
     * @return the names of the unavailable dishes, empty if all can be ordered
     */
    List<String> namesOfUnavailableDishes(List<UUID> dishIds);
    
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