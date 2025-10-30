package be.kdg.keepdishesgoing.order.port.out;

import java.util.UUID;

/**
 * Port for loading restaurant projections
 */
public interface LoadRestaurantProjectionPort {

    boolean isRestaurantOpen(UUID restaurantId);

    String getRestaurantName(UUID restaurantId);
}