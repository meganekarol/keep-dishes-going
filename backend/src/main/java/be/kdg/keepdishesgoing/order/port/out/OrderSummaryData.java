package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One stored order row, as the driven side hands it over.
 *
 * <p>Separate from {@code RestaurantOrderSummary}, which the inbound port owns.
 * The two carry the same fields today, but they answer to different sides: this
 * one follows the read model's columns, and the other is the contract the API
 * publishes. Merging them would let a change of storage reach the clients.
 */
public record OrderSummaryData(
        UUID orderId,
        OrderStatus status,
        String customerName,
        String deliveryAddress,
        String itemSummary,
        int itemCount,
        BigDecimal totalPrice,
        LocalDateTime placedAt,
        LocalDateTime acceptedAt,
        LocalDateTime readyAt,
        String rejectionReason
) {
}
