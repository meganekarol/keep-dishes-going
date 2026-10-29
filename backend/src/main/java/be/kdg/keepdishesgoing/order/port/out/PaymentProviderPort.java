package be.kdg.keepdishesgoing.order.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Talking to whoever handles the money (US-23).
 *
 * <p>Authorize at checkout, capture when the restaurant accepts, release when it
 * refuses. Nothing crossing this interface names a provider, so swapping one is
 * a new adapter and nothing else.
 */
public interface PaymentProviderPort {

    /**
     * Opens a hosted checkout for one order. Nothing is held yet when this
     * returns; the provider confirms the hold later by calling back.
     *
     * @return where to send the customer to pay
     * @throws PaymentProviderException when the provider could not be reached or
     *                                  refused the request
     */
    Checkout openCheckout(CheckoutRequest request);

    /**
     * Whether the provider is really holding money under this reference. Asked
     * before believing a callback, since anyone can send us one.
     *
     * @return false when the money is not held, including when the provider
     *         could not be reached to say either way
     */
    boolean isHeld(String paymentReference);

    /**
     * Takes money the provider is already holding.
     *
     * @param paymentReference the reference the hold was confirmed with
     * @throws PaymentProviderException when the charge did not go through, in
     *                                  which case the order must not be accepted
     */
    void capture(String paymentReference);

    /**
     * Gives a hold back without charging it.
     *
     * @throws PaymentProviderException when the provider refused, which leaves
     *                                  the hold in place until it lapses on its
     *                                  own
     */
    void release(String paymentReference);

    /**
     * @param orderId  ours, sent along so the provider quotes it back in the
     *                 callback and we know which order was paid for
     * @param amount   the order total, in whole currency units
     * @param currency ISO 4217, lowercase
     */
    record CheckoutRequest(
            UUID orderId,
            String customerEmail,
            String restaurantName,
            BigDecimal amount,
            String currency,
            List<Line> lines
    ) {

        /** One ordered dish, so the provider's page itemises what is being paid for. */
        public record Line(String dishName, BigDecimal unitPrice, int quantity) {
        }
    }

    /**
     * @param checkoutUrl the provider's payment page, to send the customer to
     * @param reference   the provider's id for this checkout, for tracing a
     *                    payment back from their dashboard to our order
     */
    record Checkout(String checkoutUrl, String reference) {
    }

    /** Raised when the provider could not be reached, or answered with a refusal. */
    class PaymentProviderException extends RuntimeException {

        public PaymentProviderException(String message) {
            super(message);
        }

        public PaymentProviderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
