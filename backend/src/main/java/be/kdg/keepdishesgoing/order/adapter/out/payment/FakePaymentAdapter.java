package be.kdg.keepdishesgoing.order.adapter.out.payment;

import be.kdg.keepdishesgoing.order.port.out.PaymentProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stands in for Stripe when no Stripe key is configured (US-23).
 *
 * <p>Lets someone run the whole order flow without a Stripe account. The
 * customer is sent to a simulated checkout page instead of Stripe's, and every
 * hold, capture and release succeeds. Exactly one of this and
 * {@link StripePaymentAdapter} is active, decided by whether
 * {@code kdg.payment.stripe.secret-key} is set.
 */
@Component
@ConditionalOnExpression("'${kdg.payment.stripe.secret-key:}'.isBlank()")
public class FakePaymentAdapter implements PaymentProviderPort {

    private static final Logger log = LoggerFactory.getLogger(FakePaymentAdapter.class);

    /**
     * References this adapter handed out. Only these count as held, so a made-up
     * reference sent to the checkout page still cannot pay for an order.
     */
    private final Set<String> issuedReferences = ConcurrentHashMap.newKeySet();

    private final String checkoutBaseUrl;

    public FakePaymentAdapter(@Value("${kdg.payment.fake.base-url}") String apiBaseUrl) {
        this.checkoutBaseUrl = apiBaseUrl + "/api/payments/fake/checkout/";
        log.warn("No Stripe key configured: payments are SIMULATED. Set kdg.payment.stripe.secret-key to use Stripe.");
    }

    @Override
    public Checkout openCheckout(CheckoutRequest request) {
        String reference = "fake_" + UUID.randomUUID();
        issuedReferences.add(reference);

        String url = UriComponentsBuilder.fromUriString(checkoutBaseUrl + request.orderId())
                .queryParam("reference", reference)
                .queryParam("amount", request.amount().toPlainString())
                .queryParam("restaurant", request.restaurantName())
                .encode()
                .toUriString();

        log.info("Simulated checkout opened for order {} ({} {})",
                request.orderId(), request.amount(), request.currency());
        return new Checkout(url, reference);
    }

    @Override
    public boolean isHeld(String paymentReference) {
        return issuedReferences.contains(paymentReference);
    }

    @Override
    public void capture(String paymentReference) {
        log.info("Simulated capture of {}", paymentReference);
    }

    @Override
    public void release(String paymentReference) {
        log.info("Simulated release of {}", paymentReference);
        issuedReferences.remove(paymentReference);
    }
}
