package be.kdg.keepdishesgoing.order.port.in.queries;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.out.OrderSummaryData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One order as its restaurant sees it (US-10, US-12).
 *
 * <p> A kitchen deciding whether to take an order needs the dishes,
 * the total and how long it has been waiting.
 *
 * @param itemSummary  the ordered dishes as one line
 * @param placedAt     when the customer checked out
 * @param pendingSince what the five-minute deadline counts from: when the order
 *                     was paid for, not when checkout started (US-23)
 */
public record RestaurantOrderSummary(
        UUID orderId,
        OrderStatus status,
        String customerName,
        String deliveryAddress,
        String itemSummary,
        int itemCount,
        BigDecimal totalPrice,
        LocalDateTime placedAt,
        LocalDateTime pendingSince,
        LocalDateTime acceptedAt,
        LocalDateTime readyAt,
        String rejectionReason
) {

    public static RestaurantOrderSummary from(OrderSummaryData data) {
        return new RestaurantOrderSummary(
                data.orderId(),
                data.status(),
                data.customerName(),
                data.deliveryAddress(),
                data.itemSummary(),
                data.itemCount(),
                data.totalPrice(),
                data.placedAt(),
                data.pendingSince(),
                data.acceptedAt(),
                data.readyAt(),
                data.rejectionReason()
        );
    }
}
