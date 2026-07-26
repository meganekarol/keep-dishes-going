package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.RejectOrderCommand;

public interface
RejectOrderUseCase {
    void rejectOrder(RejectOrderCommand command);
}
