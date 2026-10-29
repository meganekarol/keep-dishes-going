package be.kdg.keepdishesgoing.order.port.in.queries;

import java.util.UUID;

/**
 * What a customer gets back from checkout (US-22, US-23, US-24).
 *
 * @param orderId     the tracking handle, handed over before the customer is
 *                    sent off to pay so it survives them never coming back
 * @param paymentUrl  the provider's payment page to continue at
 */
public record Checkout(UUID orderId, String paymentUrl) {
}
