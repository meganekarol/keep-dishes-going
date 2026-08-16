package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.RejectStaleOrdersUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.RejectStaleOrdersCommand;
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
 * Rejects orders their restaurant never answered (US-11).
 *
 * <p>The read model picks the candidates, the aggregate makes the decision. It
 * has to work that way round: the read model is maintained by a listener and
 * can lag, so treating it as the authority would reject an order the kitchen
 * had in fact just accepted.
 */
@Service
@Transactional
public class RejectStaleOrdersUseCaseImpl implements RejectStaleOrdersUseCase {

    private static final Logger log = LoggerFactory.getLogger(RejectStaleOrdersUseCaseImpl.class);

    private final LoadOrderSummaryPort loadOrderSummaryPort;
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort saveOrderPort;
    private final OrderEventPublisherPort publishEventPort;

    public RejectStaleOrdersUseCaseImpl(
            LoadOrderSummaryPort loadOrderSummaryPort,
            LoadOrderPort loadOrderPort,
            SaveOrderPort saveOrderPort,
            OrderEventPublisherPort publishEventPort) {
        this.loadOrderSummaryPort = loadOrderSummaryPort;
        this.loadOrderPort = loadOrderPort;
        this.saveOrderPort = saveOrderPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void rejectStaleOrders(RejectStaleOrdersCommand command) {
        List<UUID> candidates = loadOrderSummaryPort.findPendingAcceptanceBefore(command.cutoff());
        if (candidates.isEmpty()) {
            return;
        }

        log.info("Auto-rejecting {} order(s) unanswered since {}", candidates.size(), command.cutoff());
        for (UUID orderId : candidates) {
            // One bad order must not strand the rest of the batch, and the next
            // run will pick it up again while it stays pending.
            try {
                rejectIfStillPending(orderId);
            } catch (Exception e) {
                log.error("Error auto-rejecting order {}", orderId, e);
            }
        }
    }

    private void rejectIfStillPending(UUID orderId) {
        Order order = loadOrderPort.loadBy(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalStateException("No order to reject: " + orderId));

        if (!order.isPendingAcceptance()) {
            log.debug("Order {} was answered after the read model was queried, leaving it alone", orderId);
            return;
        }

        order.autoReject();
        saveOrderPort.save(order);
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Order {} automatically rejected", orderId);
    }
}
