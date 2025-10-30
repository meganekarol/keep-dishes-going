package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a single line item in an order
 */
public record OrderLine(
        UUID dishId,
        String dishName,
        BigDecimal unitPrice,
        int quantity
) {

    public OrderLine {
        Objects.requireNonNull(dishId, "Dish ID cannot be null");
        Objects.requireNonNull(dishName, "Dish name cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (dishName.isBlank()) {
            throw new IllegalArgumentException("Dish name cannot be blank");
        }

        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    /**
     * Calculate the subtotal for this order line
     */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Create a new order line with a different quantity
     */
    public OrderLine withQuantity(int newQuantity) {
        return new OrderLine(dishId, dishName, unitPrice, newQuantity);
    }
}
