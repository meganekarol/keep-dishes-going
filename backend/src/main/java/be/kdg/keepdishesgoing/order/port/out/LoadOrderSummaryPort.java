package be.kdg.keepdishesgoing.order.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Output port for reading the per-restaurant order read model.
 */
public interface LoadOrderSummaryPort {

    /** Newest order first, leaving out checkouts nobody paid for (US-23). */
    List<OrderSummaryData> loadByRestaurant(UUID restaurantId);

    /**
     * Orders still waiting on their restaurant that have been in front of it
     * since before {@code cutoff}, as candidates for automatic rejection.
     *
     * <p>Only the ids: the caller loads each aggregate anyway, because the read
     * model can lag and is not allowed to decide that an order gets rejected.
     */
    List<UUID> findPendingAcceptanceBefore(LocalDateTime cutoff);

    /**
     * Checkouts started before {@code cutoff} that were never paid for (US-23).
     *
     * <p>Ids only, for the same reason as above.
     */
    List<UUID> findAwaitingPaymentBefore(LocalDateTime cutoff);

    /**
     * Orders this restaurant has taken but not yet marked ready for pickup,
     * which is the busyness factor in the delivery guesstimate (US-19).
     */
    int countNotReadyForRestaurant(UUID restaurantId);
}
