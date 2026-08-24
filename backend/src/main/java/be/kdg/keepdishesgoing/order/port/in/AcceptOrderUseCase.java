package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.AcceptOrderCommand;

/** Lets a restaurant take a new order on (US-10). */
public interface AcceptOrderUseCase {
    /**
     * Accepts an order and takes the money that was held for it.
     *
     * @param command which order to accept
     */
    void acceptOrder(AcceptOrderCommand command);
}
