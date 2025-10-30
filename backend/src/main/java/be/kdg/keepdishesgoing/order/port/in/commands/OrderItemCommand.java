package be.kdg.keepdishesgoing.order.port.in.commands;

import java.util.UUID;

/**
 * Represents a single dish and quantity requested by the customer at checkout.
 * Defined independently to preserve loose coupling between commands.
 */
public record OrderItemCommand(
        UUID dishId,
        int quantity
) {}