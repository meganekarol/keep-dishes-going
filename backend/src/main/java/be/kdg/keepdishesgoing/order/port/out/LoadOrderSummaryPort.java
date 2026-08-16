package be.kdg.keepdishesgoing.order.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for reading the per-restaurant order read model.
 */
public interface LoadOrderSummaryPort {

    /** Newest order first. */
    List<OrderSummaryData> loadByRestaurant(UUID restaurantId);

    /**
     * Orders still waiting on their restaurant that were placed before
     * {@code cutoff}, as candidates for automatic rejection.
     *
     * <p>Only the ids: the caller loads each aggregate anyway, because the read
     * model can lag and is not allowed to decide that an order gets rejected.
     */
    List<UUID> findPendingAcceptanceBefore(LocalDateTime cutoff);
}
