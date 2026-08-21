package be.kdg.keepdishesgoing.order.adapter.out.payment;

import be.kdg.keepdishesgoing.order.port.out.PaymentProviderPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Pays through Stripe Checkout, holding the money before taking it (US-23).
 *
 * <p>Sessions use {@code capture_method=manual}, so completing checkout only
 * reserves the money and {@link #capture} is what moves it. Spoken to over plain
 * HTTP rather than Stripe's SDK, so this is the only class that knows Stripe
 * exists. Amounts go over the wire in cents.
 */
@Component
public class StripePaymentAdapter implements PaymentProviderPort {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentAdapter.class);

    /** How long the customer has on Stripe's page. Stripe refuses anything under 30 minutes. */
    private static final Duration CHECKOUT_WINDOW = Duration.ofMinutes(32);

    private final RestClient stripe;
    private final ObjectMapper objectMapper;
    private final String successUrl;
    private final String cancelUrl;

    public StripePaymentAdapter(
            @Value("${kdg.payment.stripe.base-url:https://api.stripe.com}") String baseUrl,
            @Value("${kdg.payment.stripe.secret-key:}") String secretKey,
            @Value("${kdg.payment.stripe.success-url}") String successUrl,
            @Value("${kdg.payment.stripe.cancel-url}") String cancelUrl,
            ObjectMapper objectMapper) {
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
        this.objectMapper = objectMapper;

        if (secretKey.isBlank()) {
            log.warn("No Stripe secret key configured, checkout will fail until kdg.payment.stripe.secret-key is set");
        }
        this.stripe = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .requestFactory(requestFactory())
                .build();
    }

    /**
     * Timeouts because a customer is waiting on every one of these calls.
     *
     * <p>Not {@code SimpleClientHttpRequestFactory}: it streams the request body,
     * which makes {@code HttpURLConnection} discard the error body on a 401 and
     * leaves every Stripe refusal unexplained.
     */
    private static ClientHttpRequestFactory requestFactory() {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(15));
        return factory;
    }

    @Override
    public Checkout openCheckout(CheckoutRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        // Quoted back to us in the callback, and the only thing tying a Stripe
        // payment to one of our orders.
        form.add("client_reference_id", request.orderId().toString());
        form.add("customer_email", request.customerEmail());
        form.add("success_url", successUrl.replace("{orderId}", request.orderId().toString()));
        form.add("cancel_url", cancelUrl.replace("{orderId}", request.orderId().toString()));
        form.add("expires_at", String.valueOf(
                System.currentTimeMillis() / 1000 + CHECKOUT_WINDOW.toSeconds()));

        // The hold. Without this Stripe charges the card outright and there is
        // nothing left to decide when the restaurant answers.
        form.add("payment_intent_data[capture_method]", "manual");
        form.add("payment_intent_data[description]",
                "KDG order at " + request.restaurantName());

        addLines(form, request);

        StripeSession session = post("/v1/checkout/sessions", form, StripeSession.class);
        if (session == null || session.url() == null) {
            throw new PaymentProviderException("Stripe returned a checkout session with no payment page");
        }

        log.info("Opened Stripe checkout {} for order {}", session.id(), request.orderId());
        return new Checkout(session.url(), session.id());
    }

    /**
     * Itemises the basket on Stripe's page. Stripe totals these lines itself, so
     * the order total is checked against them rather than sent.
     */
    private static void addLines(MultiValueMap<String, String> form, CheckoutRequest request) {
        List<CheckoutRequest.Line> lines = request.lines();
        long charged = 0;

        for (int i = 0; i < lines.size(); i++) {
            CheckoutRequest.Line line = lines.get(i);
            long unitAmount = toCents(line.unitPrice());
            charged += unitAmount * line.quantity();

            String prefix = "line_items[" + i + "]";
            form.add(prefix + "[quantity]", String.valueOf(line.quantity()));
            form.add(prefix + "[price_data][currency]", request.currency());
            form.add(prefix + "[price_data][unit_amount]", String.valueOf(unitAmount));
            form.add(prefix + "[price_data][product_data][name]", line.dishName());
        }

        long owed = toCents(request.amount());
        if (charged != owed) {
            throw new PaymentProviderException(
                    "Refusing to charge " + charged + " cents for an order that costs " + owed);
        }
    }

    /** @throws PaymentProviderException on a price that is not a whole number of cents */
    private static long toCents(BigDecimal amount) {
        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new PaymentProviderException("Cannot charge " + amount + ", it is not a whole number of cents", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Idempotent: a refusal is read back, and a payment Stripe has already
     * captured counts as done. The owner's retry has to be able to finish the
     * job rather than jam on an error.
     */
    @Override
    public void capture(String paymentReference) {
        try {
            StripePaymentIntent intent = post(
                    "/v1/payment_intents/" + paymentReference + "/capture",
                    new LinkedMultiValueMap<>(),
                    StripePaymentIntent.class);

            if (intent == null || !"succeeded".equals(intent.status())) {
                throw new PaymentProviderException(
                        "Stripe did not take the money for " + paymentReference
                                + ", it answered " + (intent == null ? "nothing" : intent.status()));
            }
        } catch (PaymentProviderException e) {
            if (!hasStatus(paymentReference, "succeeded")) {
                throw e;
            }
            log.info("Stripe had already captured {}, treating the retry as done", paymentReference);
            return;
        }
        log.info("Captured Stripe payment {}", paymentReference);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Safe to call twice, for the same reason as {@link #capture}.
     */
    @Override
    public void release(String paymentReference) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("cancellation_reason", "abandoned");

        try {
            post("/v1/payment_intents/" + paymentReference + "/cancel", form, StripePaymentIntent.class);
        } catch (PaymentProviderException e) {
            if (!hasStatus(paymentReference, "canceled")) {
                throw e;
            }
            log.info("Stripe had already released {}, treating the retry as done", paymentReference);
            return;
        }
        log.info("Released Stripe hold {}", paymentReference);
    }

    /** @return false when Stripe cannot be reached, never an optimistic guess */
    private boolean hasStatus(String paymentReference, String expected) {
        return expected.equals(readStatus(paymentReference));
    }

    /**
     * {@inheritDoc}
     *
     * <p>With manual capture a completed session sits at {@code requires_capture},
     * not {@code succeeded}.
     */
    @Override
    public boolean isHeld(String paymentReference) {
        return "requires_capture".equals(readStatus(paymentReference));
    }

    /** @return Stripe's own word for where the payment is, or {@code null} if it could not be asked */
    private String readStatus(String paymentReference) {
        try {
            StripePaymentIntent intent = stripe.get()
                    .uri("/v1/payment_intents/{id}", paymentReference)
                    .retrieve()
                    .body(StripePaymentIntent.class);
            return intent == null ? null : intent.status();
        } catch (Exception e) {
            log.warn("Could not read Stripe payment {}: {}", paymentReference, e.getMessage());
            return null;
        }
    }

    private <T> T post(String path, MultiValueMap<String, String> form, Class<T> responseType) {
        try {
            return stripe.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            // Stripe explains itself in the body, and the status alone is not
            // enough to tell a bad key from a card that was declined.
            String reason = explain(e);
            log.error("Stripe refused {} with {}: {}", path, e.getStatusCode(), reason);
            throw new PaymentProviderException("Stripe refused the request: " + reason, e);
        } catch (Exception e) {
            throw new PaymentProviderException("Could not reach Stripe", e);
        }
    }

    /** Stripe's own wording for a refusal, falling back to the body then the status line. */
    private String explain(RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        try {
            JsonNode message = objectMapper.readTree(body).path("error").path("message");
            if (message.isTextual() && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (JacksonException notTheDocumentedShape) {
            // Deliberately swallowed: the raw body below beats losing the
            // reason to a parse failure.
        }
        return body.isBlank() ? e.getStatusText() : body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StripeSession(String id, String url) {
    }

    /**
     * @param status {@code requires_capture} while the money is held,
     *               {@code succeeded} once taken, {@code canceled} once released
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record StripePaymentIntent(String id, String status) {
    }
}
