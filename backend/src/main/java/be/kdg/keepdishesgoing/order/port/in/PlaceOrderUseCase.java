package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;

public interface PlaceOrderUseCase {
    OrderId placeOrder(PlaceOrderCommand command);
}
