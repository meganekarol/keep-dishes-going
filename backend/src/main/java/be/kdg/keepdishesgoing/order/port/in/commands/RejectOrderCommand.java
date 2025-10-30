package be.kdg.keepdishesgoing.order.port.in.commands;

import java.util.UUID;

/**
 * Command for rejecting an order manually with a reason.
 */
public record RejectOrderCommand(
        UUID orderId,
        UUID restaurantId,
        String reason
) {

    public RejectOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be blank");
        }
    }
}
