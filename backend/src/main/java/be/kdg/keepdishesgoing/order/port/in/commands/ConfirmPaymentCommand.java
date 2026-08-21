package be.kdg.keepdishesgoing.order.port.in.commands;

import java.util.UUID;

/**
 * @param orderId          which order was paid for, quoted back to us by the
 *                         provider from what we sent when opening the checkout
 * @param paymentReference the provider's id for the hold, needed later to take
 *                         the money or give it back
 */
public record ConfirmPaymentCommand(UUID orderId, String paymentReference) {
}
