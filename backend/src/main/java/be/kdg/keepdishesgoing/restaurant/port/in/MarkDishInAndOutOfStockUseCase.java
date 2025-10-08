package be.kdg.keepdishesgoing.restaurant.port.in;

public interface MarkDishInAndOutOfStockUseCase {
    void markDishInStock(MarkDishInStockCommand command);
    void markDishOutOfStock(MarkDishOutOfStockCommand command);
}
