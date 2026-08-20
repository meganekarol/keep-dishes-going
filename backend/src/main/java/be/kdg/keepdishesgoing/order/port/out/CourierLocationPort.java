package be.kdg.keepdishesgoing.order.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * The last known position of the courier carrying an order (US-29).
 */
public interface CourierLocationPort {

    /**
     * @return {@code true} when this is the first position ever reported for
     *         the order, which is what tells us a courier has actually set off
     */
    boolean record(UUID orderId, double lat, double lng, LocalDateTime reportedAt);

    Optional<CourierLocation> findByOrder(UUID orderId);

    record CourierLocation(double lat, double lng, LocalDateTime reportedAt) {
    }
}
