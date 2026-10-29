package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.ExpireAbandonedCheckoutsUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.ExpireAbandonedCheckoutsCommand;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderSummaryPort;
import be.kdg.keepdishesgoing.order.port.out.OrderEventPublisherPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Abandons checkouts nobody paid for (US-23).
 *
 * <p>The read model picks candidates and the aggregate decides, since a
 * projection can lag. No money is touched: there is no hold to give back.
 */
@Service
@Transactional
public class ExpireAbandonedCheckoutsUseCaseImpl implements ExpireAbandonedCheckoutsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExpireAbandonedCheckoutsUseCaseImpl.class);

    private final LoadOrderSummaryPort loadOrderSummaryPort;
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderEventPublisherPort publishEventPort;

    public ExpireAbandonedCheckoutsUseCaseImpl(LoadOrderSummaryPort loadOrderSummaryPort,
                                               LoadOrderPort loadOrderPort,
                                               SaveOrderPort saveOrderPort,
                                               OrderEventPublisherPort publishEventPort) {
        this.loadOrderSummaryPort = loadOrderSummaryPort;
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public int expireAbandonedCheckouts(ExpireAbandonedCheckoutsCommand command) {
        List<UUID> candidates = loadOrderSummaryPort.findAwaitingPaymentBefore(command.cutoff());
        if (candidates.isEmpty()) {
            return 0;
        }

        log.info("Abandoning {} checkout(s) unpaid since {}", candidates.size(), command.cutoff());
        int expired = 0;
        for (UUID orderId : candidates) {
            // One bad order must not strand the rest of the batch, and the next
            // run picks it up again while it stays unpaid.
            try {
                if (expireIfStillUnpaid(orderId)) {
                    expired++;
                }
            } catch (Exception e) {
                log.error("Error abandoning checkout {}", orderId, e);
            }
        }
        return expired;
    }

    private boolean expireIfStillUnpaid(UUID orderId) {
        Order order = loadOrderPort.loadBy(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalStateException("No order to abandon: " + orderId));

        if (!order.isAwaitingPayment()) {
            log.debug("Order {} was paid for after the read model was queried, leaving it alone", orderId);
            return false;
        }

        order.expirePayment();
        saveOrderPort.save(order);
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Checkout {} abandoned, it was never paid for", orderId);
        return true;
    }
}
