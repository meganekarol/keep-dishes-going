package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * One dish on an order, with the name and price copied from the menu at checkout.
 *
 * <p>Copying them means a later price change cannot rewrite what the customer
 * already agreed to pay.
 *
 * @param dishId    which dish was ordered
 * @param dishName  what it was called at the time
 * @param unitPrice what one of them cost at the time
 * @param quantity  how many were ordered
 */
public record OrderLine(
        UUID dishId,
        String dishName,
        BigDecimal unitPrice,
        int quantity
) {

    /**
     * Checks the line makes sense.
     *
     * @throws NullPointerException     if the id, name or price is missing
     * @throws IllegalArgumentException if the name is blank, or the price or
     *                                  quantity is not above zero
     */
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
     * Works out what this line costs in total.
     *
     * @return the unit price times the quantity
     */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Returns a copy of this line with a different quantity.
     *
     * @param newQuantity how many are wanted now
     * @return a new line for the same dish with that quantity
     */
    public OrderLine withQuantity(int newQuantity) {
        return new OrderLine(dishId, dishName, unitPrice, newQuantity);
    }
}
