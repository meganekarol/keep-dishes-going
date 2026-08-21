package be.kdg.keepdishesgoing.order.adapter.in.payment;

import be.kdg.keepdishesgoing.order.port.in.ConfirmPaymentUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.ConfirmPaymentCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Where Stripe tells us what happened to a payment (US-23).
 *
 * <p>Needed on top of the customer's redirect, which only happens if they wait
 * for it. Read as a tree rather than a record, since Stripe's payload grows
 * between API versions.
 */
@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private static final String CHECKOUT_COMPLETED = "checkout.session.completed";
    private static final String CHECKOUT_EXPIRED = "checkout.session.expired";

    private final StripeSignatureVerifier signatureVerifier;
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StripeWebhookController(StripeSignatureVerifier signatureVerifier,
                                   ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.signatureVerifier = signatureVerifier;
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    /**
     * @param payload the raw body; re-serialising it would break the signature
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onStripeEvent(@RequestBody String payload,
                                              @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        if (!signatureVerifier.isSignatureValid(payload, signature)) {
            // Deliberately terse. Anything more tells whoever is probing this
            // endpoint how close their forgery was.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JsonNode event = objectMapper.readTree(payload);
            String type = event.path("type").asText();
            JsonNode session = event.path("data").path("object");

            switch (type) {
                case CHECKOUT_COMPLETED -> confirmPayment(session);
                case CHECKOUT_EXPIRED -> log.info(
                        "Stripe expired checkout for order {}, the scheduled sweep will abandon it",
                        session.path("client_reference_id").asText());
                default -> log.debug("Ignoring Stripe event {}", type);
            }
        } catch (Exception e) {
            // Answering with an error makes Stripe redeliver. Worth it for a
            // transient fault, but a payload we can never parse would come back
            // forever, so it is logged and accepted.
            log.error("Could not handle a Stripe callback", e);
        }

        // Always 200 once the signature checks out. Stripe reads anything else
        // as "not delivered" and retries for days.
        return ResponseEntity.ok().build();
    }

    private void confirmPayment(JsonNode session) {
        String orderId = session.path("client_reference_id").asText(null);
        String paymentIntent = session.path("payment_intent").asText(null);

        if (orderId == null || paymentIntent == null) {
            log.warn("Stripe reported a completed checkout with no order id or payment attached");
            return;
        }

        confirmPaymentUseCase.confirmPayment(
                new ConfirmPaymentCommand(UUID.fromString(orderId), paymentIntent));
    }
}
