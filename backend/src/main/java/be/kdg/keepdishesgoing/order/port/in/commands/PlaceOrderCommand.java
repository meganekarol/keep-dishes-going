package be.kdg.keepdishesgoing.order.port.in.commands;

import be.kdg.keepdishesgoing.order.domain.valueObjects.*;

import java.util.List;
import java.util.UUID;

/**
 * Command for placing a new order at checkout.
 * This is initiated by the customer when confirming the basket.
 *
 * <p>The customer only states <em>what</em> they want ({@link OrderItemCommand}:
 * dish + quantity). Dish names and prices are resolved by the use case from the
 * dish projection, so the price the order is fixed at is the one the platform
 * holds - never one supplied by the client.
 */
public record PlaceOrderCommand(
        UUID restaurantId,
        CustomerInfo customerInfo,
        DeliveryAddress deliveryAddress,
        List<OrderItemCommand> items
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
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (OrderItemCommand item : items) {
            if (item == null || item.dishId() == null) {
                throw new IllegalArgumentException("Every order item must reference a dish");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }
        // Make immutable copy
        items = List.copyOf(items);
    }
}
