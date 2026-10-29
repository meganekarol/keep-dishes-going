package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.RejectOrderCommand;

/** Lets a restaurant turn an order away, with a reason (US-10). */
public interface RejectOrderUseCase {

    /**
     * Rejects an order and gives the held money back.
     *
     * @param command which order to reject and why
     */
    void rejectOrder(RejectOrderCommand command);
}
