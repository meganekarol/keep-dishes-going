package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.ConfirmPaymentCommand;

/**
 * Driven by the payment provider's callback (US-23).
 *
 * <p>An inbound port of its own rather than a branch of placing an order: the
 * caller is the provider, not the customer, and it arrives long after the
 * request that started the checkout has been answered.
 */
public interface ConfirmPaymentUseCase {

    /**
     * Records that the money for an order is now held, which is what puts the
     * order in front of its restaurant.
     *
     * <p>Must tolerate being called twice with the same payment. Providers
     * retry a callback they did not get an answer to.
     */
    void confirmPayment(ConfirmPaymentCommand command);
}
