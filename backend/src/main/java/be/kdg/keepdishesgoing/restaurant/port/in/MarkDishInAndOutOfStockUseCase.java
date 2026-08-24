package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishInStockCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.MarkDishOutOfStockCommand;

/** Lets an owner say a dish has sold out, or come back (US-08). */
public interface MarkDishInAndOutOfStockUseCase {
    /**
     * Puts a sold-out dish back on sale.
     *
     * @param command which dish is back in stock
     */
    void markDishInStock(MarkDishInStockCommand command);
    /**
     * Marks a dish sold out. It stays on the menu but cannot be ordered.
     *
     * @param command which dish has run out
     */
    void markDishOutOfStock(MarkDishOutOfStockCommand command);
}
