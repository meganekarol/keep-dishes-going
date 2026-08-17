package be.kdg.keepdishesgoing.order.port.in.queries;

import be.kdg.keepdishesgoing.common.DeliveryEstimate;
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
 * @param estimatedDeliveryAt
 *                        roughly when the food should arrive (US-19), or
 *                        {@code null} once the order has stopped moving.
 *                        Counted from acceptance where there is one, because
 *                        the kitchen's clock only starts when it says yes
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
        LocalDateTime deliveredAt,
        LocalDateTime estimatedDeliveryAt
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

    /**
     * @param preparationMinutes the restaurant's own estimate, {@code 0} when
     *                           it was never recorded
     * @param ordersNotReady     how many orders that kitchen still has to cook
     */
    public static OrderTracking from(Order order, String restaurantName,
                                     int preparationMinutes, int ordersNotReady) {
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
                order.getDeliveredAt(),
                estimateFor(order, preparationMinutes, ordersNotReady)
        );
    }

    /**
     * Nothing is quoted once the order has stopped moving: a delivered order
     * has a real time on it, and a rejected one is never coming.
     *
     * <p>The distance is left unknown here. The customer's browser position was
     * only ever available while they were choosing a restaurant, and where they
     * happen to be standing now is not where the food is going.
     */
    private static LocalDateTime estimateFor(Order order, int preparationMinutes, int ordersNotReady) {
        if (order.getStatus().isTerminal()) {
            return null;
        }
        LocalDateTime startedFrom = order.getAcceptedAt() != null
                ? order.getAcceptedAt()
                : order.getPlacedAt();
        return startedFrom.plusMinutes(
                DeliveryEstimate.totalMinutes(null, preparationMinutes, ordersNotReady));
    }
}
