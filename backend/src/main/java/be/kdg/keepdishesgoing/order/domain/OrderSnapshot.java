package be.kdg.keepdishesgoing.order.domain;

import be.kdg.keepdishesgoing.order.domain.valueObjects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A saved copy of an order's state, taken every fifty events.
 *
 * <p>Rebuilding an order starts from the newest snapshot and replays only the
 * events after it, instead of replaying the whole history.
 *
 * @param orderId         which order this is a snapshot of
 * @param customerInfo    who placed it
 * @param restaurantId    which restaurant it is for
 * @param deliveryAddress where it is going
 * @param orderLines      the dishes on it
 * @param totalPrice      what it comes to
 * @param status          where the order was at this point
 * @param placedAt        when it was placed
 * @param acceptedAt      when the restaurant accepted it, or {@code null}
 * @param readyAt         when it was ready for pickup, or {@code null}
 * @param pickedUpAt      when a courier collected it, or {@code null}
 * @param deliveredAt     when it arrived, or {@code null}
 * @param rejectionReason why it was turned away, or {@code null}
 * @param payment         where the money is (US-23), {@code null} on snapshots
 *                        taken before payment existed
 * @param version         how many events had been applied at this point
 */
public record OrderSnapshot(
    UUID orderId,
    CustomerInfo customerInfo,
    RestaurantId restaurantId,
    DeliveryAddress deliveryAddress,
    List<OrderLine> orderLines,
    BigDecimal totalPrice,
    OrderStatus status,
    LocalDateTime placedAt,
    LocalDateTime acceptedAt,
    LocalDateTime readyAt,
    LocalDateTime pickedUpAt,
    LocalDateTime deliveredAt,
    String rejectionReason,
    Payment payment,
    int version
) {

    /** Copies the order lines so the snapshot cannot be changed afterwards. */
    public OrderSnapshot {
        orderLines = List.copyOf(orderLines);
    }
}
