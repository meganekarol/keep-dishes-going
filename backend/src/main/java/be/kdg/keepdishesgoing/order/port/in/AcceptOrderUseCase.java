package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.AcceptOrderCommand;

public interface AcceptOrderUseCase {
    void acceptOrder(AcceptOrderCommand command);
}
