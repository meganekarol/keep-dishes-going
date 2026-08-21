package be.kdg.keepdishesgoing.order.adapter.in.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Proves a callback really came from Stripe (US-23).
 *
 * <p>The endpoint is open to the internet, so without this anyone could claim an
 * order was paid for. Stripe signs {@code t + "." + body} with a shared secret
 * and sends the result as {@code Stripe-Signature: t=...,v1=...}.
 */
@Component
public class StripeSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(StripeSignatureVerifier.class);

    private static final String ALGORITHM = "HmacSHA256";

    /** How stale a signed callback may be, so a captured one cannot be replayed later. */
    private static final Duration TOLERANCE = Duration.ofMinutes(5);

    private final String signingSecret;

    public StripeSignatureVerifier(@Value("${kdg.payment.stripe.webhook-secret:}") String signingSecret) {
        this.signingSecret = signingSecret;
        if (signingSecret.isBlank()) {
            log.warn("No Stripe webhook secret configured, every payment callback will be refused");
        }
    }

    /**
     * @param payload the body exactly as it arrived; re-serialising it first
     *                changes the bytes and the signature stops matching
     * @param header  the value of the {@code Stripe-Signature} header
     */
    public boolean isSignatureValid(String payload, String header) {
        if (signingSecret.isBlank() || header == null || header.isBlank()) {
            return false;
        }

        String timestamp = null;
        String expected = null;
        for (String part : header.split(",")) {
            String[] pair = part.trim().split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            if ("t".equals(pair[0])) {
                timestamp = pair[1];
            } else if ("v1".equals(pair[0])) {
                expected = pair[1];
            }
        }

        if (timestamp == null || expected == null) {
            log.warn("Stripe signature header is missing its timestamp or signature");
            return false;
        }
        if (isTooOld(timestamp)) {
            log.warn("Refusing a Stripe callback signed at {}, outside the {} minute window",
                    timestamp, TOLERANCE.toMinutes());
            return false;
        }

        String actual = sign(timestamp + "." + payload);
        // Constant time: a plain equals leaks how much of a guessed signature
        // was right, one character at a time.
        boolean valid = MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            log.warn("Refusing a Stripe callback whose signature does not match");
        }
        return valid;
    }

    private static boolean isTooOld(String timestamp) {
        try {
            Instant signedAt = Instant.ofEpochSecond(Long.parseLong(timestamp));
            return Duration.between(signedAt, Instant.now()).abs().compareTo(TOLERANCE) > 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String sign(String signedPayload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign the Stripe payload", e);
        }
    }
}
