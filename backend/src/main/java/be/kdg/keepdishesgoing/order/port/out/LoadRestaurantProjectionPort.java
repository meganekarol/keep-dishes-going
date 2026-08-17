package be.kdg.keepdishesgoing.order.port.out;

import java.util.UUID;

/**
 * Port for loading restaurant projections
 */
public interface LoadRestaurantProjectionPort {

    boolean isRestaurantOpen(UUID restaurantId);

    String getRestaurantName(UUID restaurantId);

    /**
     * Minutes the kitchen asks for, or {@code 0} when it was never recorded.
     * Feeds the delivery guess on the tracking page (US-19).
     */
    int getPreparationTime(UUID restaurantId);
}