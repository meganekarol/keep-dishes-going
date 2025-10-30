package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.OrderId;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;

public interface PlaceOrderUseCase {
    OrderId placeOrder(PlaceOrderCommand command);
}
