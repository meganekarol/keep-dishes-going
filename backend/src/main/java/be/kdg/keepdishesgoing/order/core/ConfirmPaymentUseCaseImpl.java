package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.ConfirmPaymentUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.ConfirmPaymentCommand;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.OrderEventPublisherPort;
import be.kdg.keepdishesgoing.order.port.out.PaymentProviderPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lets an order through to its restaurant once the money behind it is held
 * (US-23).
 *
 * <p>The callback is treated as a hint, not proof: the provider is asked
 * directly before anything is recorded.
 */
@Service
@Transactional
public class ConfirmPaymentUseCaseImpl implements ConfirmPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConfirmPaymentUseCaseImpl.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderEventPublisherPort publishEventPort;
    private final PaymentProviderPort paymentProviderPort;

    public ConfirmPaymentUseCaseImpl(LoadOrderPort loadOrderPort,
                                     SaveOrderPort saveOrderPort,
                                     OrderEventPublisherPort publishEventPort,
                                     PaymentProviderPort paymentProviderPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.publishEventPort = publishEventPort;
        this.paymentProviderPort = paymentProviderPort;
    }

    @Override
    public void confirmPayment(ConfirmPaymentCommand command) {
        Order order = loadOrderPort.loadBy(OrderId.of(command.orderId()))
                .orElseThrow(() -> new NotFoundException(
                        "No order to confirm payment for: " + command.orderId()));

        // A repeat of a callback already handled. Cheap to spot, and worth
        // spotting before spending a call on the provider.
        if (!order.isAwaitingPayment()) {
            log.debug("Order {} is already {}, ignoring a repeated payment callback",
                    command.orderId(), order.getStatus());
            return;
        }

        if (!paymentProviderPort.isHeld(command.paymentReference())) {
            log.warn("Payment {} for order {} is not held by the provider, leaving the order unpaid",
                    command.paymentReference(), command.orderId());
            return;
        }

        if (!order.authorizePayment(command.paymentReference())) {
            return;
        }

        saveOrderPort.save(order);
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Order {} is paid for and now waiting on restaurant {}",
                command.orderId(), order.getRestaurantId().uuid());
    }
}
