package be.kdg.keepdishesgoing.order.domain;

import be.kdg.keepdishesgoing.order.domain.valueObjects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Snapshot of an Order's state for event sourcing optimization
 * Allows rebuilding order state without replaying all events
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
    /** Where the order's money is (US-23), absent on snapshots taken before payment existed. */
    Payment payment,
    int version
) {
    
    public OrderSnapshot {
        // Ensure immutability by copying the list
        orderLines = List.copyOf(orderLines);
    }
}