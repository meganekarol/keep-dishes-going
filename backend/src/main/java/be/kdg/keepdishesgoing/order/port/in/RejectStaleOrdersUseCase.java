package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.RejectStaleOrdersCommand;

/**
 * Turns away orders a restaurant left unanswered for too long.
 */
public interface RejectStaleOrdersUseCase {

    void rejectStaleOrders(RejectStaleOrdersCommand command);
}
