package be.kdg.keepdishesgoing.order.port.in.commands;

import java.util.UUID;

/**
 * Command for accepting an order by the restaurant owner.
 */
public record AcceptOrderCommand(
        UUID orderId,
        UUID restaurantId
) {

    public AcceptOrderCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }
}
