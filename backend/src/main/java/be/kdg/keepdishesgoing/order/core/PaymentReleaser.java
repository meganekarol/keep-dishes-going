package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.port.out.PaymentProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Gives a hold back to the customer (US-23).
 *
 * <p>Shared by the two ways an order gets turned down: a restaurant refusing it,
 * and nobody answering it in time.
 */
@Component
class PaymentReleaser {

    private static final Logger log = LoggerFactory.getLogger(PaymentReleaser.class);

    private final PaymentProviderPort paymentProviderPort;

    PaymentReleaser(PaymentProviderPort paymentProviderPort) {
        this.paymentProviderPort = paymentProviderPort;
    }

    /**
     * Releases the order's hold, if it has one. A provider that refuses the
     * instruction does not block the rejection; the hold lapses on its own.
     */
    void releaseIfHeld(Order order) {
        if (!order.hasHeldPayment()) {
            return;
        }
        try {
            paymentProviderPort.release(order.getPaymentReference());
            order.recordPaymentReleased();
        } catch (PaymentProviderPort.PaymentProviderException e) {
            log.error("Could not release the hold on order {} ({}), it will lapse on its own",
                    order.getOrderId().uuid(), order.getPaymentReference(), e);
        }
    }
}
