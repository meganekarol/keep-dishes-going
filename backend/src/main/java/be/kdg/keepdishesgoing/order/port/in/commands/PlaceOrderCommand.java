package be.kdg.keepdishesgoing.order.port.in.commands;

import be.kdg.keepdishesgoing.order.domain.valueObjects.*;

import java.util.List;
import java.util.UUID;

/**
 * Command for placing a new order at checkout.
 * This is initiated by the customer when confirming the basket.
 */
public record PlaceOrderCommand(
        UUID restaurantId,
        CustomerInfo customerInfo,
        DeliveryAddress deliveryAddress,
        List<OrderLine> orderLines
) {

    public PlaceOrderCommand {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (customerInfo == null) {
            throw new IllegalArgumentException("Customer info cannot be null");
        }
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one order line");
        }
        // Make immutable copy
        orderLines = List.copyOf(orderLines);
    }
}
