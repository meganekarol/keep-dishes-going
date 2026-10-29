package be.kdg.keepdishesgoing.order.port.in.commands;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;

import java.util.UUID;

/**
 * Command for updating order status based on delivery events
 */
public record UpdateOrderStatusCommand(
    UUID orderId,
    OrderStatus newStatus
) {
    
    public UpdateOrderStatusCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
    }
}