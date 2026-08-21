package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.queries.Checkout;

public interface PlaceOrderUseCase {

    /**
     * Records the order and opens a payment for it (US-22, US-23).
     *
     * <p>The order exists when this returns, but no restaurant has been told
     * about it: that only happens once the payment provider confirms the money
     * is held.
     */
    Checkout placeOrder(PlaceOrderCommand command);
}
