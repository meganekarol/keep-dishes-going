package be.kdg.keepdishesgoing.order.port.in.queries;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderLine;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * What a customer sees on the tracking page (US-24, US-25).
 *
 * <p>The five timestamps are the progress bar. Each one is {@code null} until
 * the matching step happens, so the client renders the timeline by reading them
 * rather than by interpreting {@code status}, which only ever tells it where
 * the order is now, not how it got there.
 *
 * @param rejectionReason set only when {@code status} is
 *                        {@link OrderStatus#REJECTED}; a customer who is turned
 *                        away is owed the reason the restaurant gave
 */
public record OrderTracking(
        UUID orderId,
        UUID restaurantId,
        String restaurantName,
        OrderStatus status,
        String rejectionReason,
        String customerName,
        DeliveryAddress deliveryAddress,
        List<Line> lines,
        BigDecimal totalPrice,
        LocalDateTime placedAt,
        LocalDateTime acceptedAt,
        LocalDateTime readyAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt
) {

    /** One ordered dish, priced as it was when the order was placed. */
    public record Line(
            UUID dishId,
            String dishName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {
        static Line from(OrderLine orderLine) {
            return new Line(
                    orderLine.dishId(),
                    orderLine.dishName(),
                    orderLine.unitPrice(),
                    orderLine.quantity(),
                    orderLine.subtotal()
            );
        }
    }

    public static OrderTracking from(Order order, String restaurantName) {
        return new OrderTracking(
                order.getOrderId().uuid(),
                order.getRestaurantId().uuid(),
                restaurantName,
                order.getStatus(),
                order.getRejectionReason(),
                order.getCustomerInfo().customerName(),
                order.getDeliveryAddress(),
                order.getOrderLines().stream().map(Line::from).toList(),
                order.getTotalPrice(),
                order.getPlacedAt(),
                order.getAcceptedAt(),
                order.getReadyAt(),
                order.getPickedUpAt(),
                order.getDeliveredAt()
        );
    }
}
