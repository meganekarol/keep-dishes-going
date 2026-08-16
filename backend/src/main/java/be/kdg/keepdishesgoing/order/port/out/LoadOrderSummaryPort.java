package be.kdg.keepdishesgoing.order.port.out;

import java.util.List;
import java.util.UUID;

/**
 * Output port for reading the per-restaurant order read model.
 */
public interface LoadOrderSummaryPort {

    /** Newest order first. */
    List<OrderSummaryData> loadByRestaurant(UUID restaurantId);
}
