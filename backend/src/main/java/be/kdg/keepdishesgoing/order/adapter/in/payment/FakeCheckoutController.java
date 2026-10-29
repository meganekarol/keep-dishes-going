package be.kdg.keepdishesgoing.order.adapter.in.payment;

import be.kdg.keepdishesgoing.order.port.in.ConfirmPaymentUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.ConfirmPaymentCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

/**
 * The simulated checkout page, the counterpart of {@code FakePaymentAdapter}
 * (US-23).
 *
 * <p>Plays both of Stripe's roles: the page the customer pays on, and the
 * callback that confirms the payment. Paying here goes through the same
 * {@link ConfirmPaymentUseCase} as a real Stripe webhook, so the order flow
 * after it is the real one.
 */
@RestController
@RequestMapping("/api/payments/fake/checkout/{orderId}")
@ConditionalOnExpression("'${kdg.payment.stripe.secret-key:}'.isBlank()")
public class FakeCheckoutController {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final String successUrl;
    private final String cancelUrl;

    public FakeCheckoutController(ConfirmPaymentUseCase confirmPaymentUseCase,
                                  @Value("${kdg.payment.stripe.success-url}") String successUrl,
                                  @Value("${kdg.payment.stripe.cancel-url}") String cancelUrl) {
        this.confirmPaymentUseCase = confirmPaymentUseCase;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String checkoutPage(@PathVariable UUID orderId,
                               @RequestParam String reference,
                               @RequestParam(defaultValue = "") String amount,
                               @RequestParam(defaultValue = "") String restaurant) {
        String action = "/api/payments/fake/checkout/" + orderId;
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Simulated checkout</title>
                  <style>
                    body { font-family: system-ui, sans-serif; background: #f4f4f5; margin: 0;
                           min-height: 100vh; display: grid; place-items: center; }
                    main { background: #fff; padding: 2rem; border-radius: 12px; max-width: 24rem;
                           box-shadow: 0 2px 12px rgba(0,0,0,.08); }
                    .notice { background: #fef3c7; padding: .75rem; border-radius: 8px; font-size: .9rem; }
                    .amount { font-size: 2rem; font-weight: 600; margin: 1rem 0; }
                    button { font: inherit; padding: .6rem 1.2rem; border-radius: 8px; border: 0; cursor: pointer; }
                    .pay { background: #16a34a; color: #fff; }
                    .cancel { background: #e4e4e7; }
                    form { display: inline; }
                  </style>
                </head>
                <body>
                <main>
                  <p class="notice">Simulated payment. No Stripe key is configured, so no money moves.
                    Set one to use Stripe's real test checkout.</p>
                  <h1>%s</h1>
                  <p class="amount">&euro; %s</p>
                  <form method="post" action="%s/pay">
                    <input type="hidden" name="reference" value="%s">
                    <button class="pay" type="submit">Pay</button>
                  </form>
                  <form method="post" action="%s/cancel">
                    <button class="cancel" type="submit">Cancel</button>
                  </form>
                </main>
                </body>
                </html>
                """.formatted(htmlEscape(restaurant), htmlEscape(amount), action, htmlEscape(reference), action);
    }

    @PostMapping("/pay")
    public ResponseEntity<Void> pay(@PathVariable UUID orderId, @RequestParam String reference) {
        confirmPaymentUseCase.confirmPayment(new ConfirmPaymentCommand(orderId, reference));
        return redirect(successUrl, orderId);
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID orderId) {
        return redirect(cancelUrl, orderId);
    }

    private static ResponseEntity<Void> redirect(String template, UUID orderId) {
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, URI.create(template.replace("{orderId}", orderId.toString())).toString())
                .build();
    }
}
