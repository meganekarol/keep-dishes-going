package be.kdg.keepdishesgoing.order.adapter.in;

import be.kdg.keepdishesgoing.common.events.*;
import be.kdg.keepdishesgoing.order.adapter.out.projections.OrderSummaryJpaEntity;
import be.kdg.keepdishesgoing.order.adapter.out.projections.OrderSummaryJpaRepository;
import be.kdg.keepdishesgoing.order.port.out.OrderSummaryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Keeps the per-restaurant order read model in step with the order lifecycle.
 *
 * <p>Unlike {@code RestaurantProjectionListener} this projects the order
 * module's own events. It exists because the aggregate is event sourced: the
 * owner's screen needs a list, and rebuilding every order to produce one does
 * not scale past a demo.
 */
@Component
public class OrderSummaryProjectionListener {

    private static final Logger log = LoggerFactory.getLogger(OrderSummaryProjectionListener.class);

    private final OrderSummaryJpaRepository summaryRepository;

    public OrderSummaryProjectionListener(OrderSummaryJpaRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @EventListener
    @Transactional
    public void on(OrderPlacedEvent event) {
        log.info("Updating projection: order {} placed at restaurant {}",
                event.orderId(), event.restaurantId());

        List<OrderSummaryData.ItemLine> lines = event.lines().stream()
                .map(line -> new OrderSummaryData.ItemLine(line.dishName(), line.quantity()))
                .toList();

        summaryRepository.save(new OrderSummaryJpaEntity(
                event.orderId(),
                event.restaurantId(),
                event.customerName(),
                OrderSummaryData.describeAddress(
                        event.street(), event.number(), event.postalCode(), event.city()),
                OrderSummaryData.describeItems(lines),
                OrderSummaryData.countItems(lines),
                event.totalPrice(),
                event.occurredAt()
        ));
    }

    /**
     * The order becomes the restaurant's to answer, and its five-minute clock
     * starts here rather than at checkout (US-23).
     */
    @EventListener
    @Transactional
    public void on(OrderPaymentAuthorizedEvent event) {
        update(event.orderId(), summary -> summary.markPendingAcceptance(event.occurredAt()));
    }

    @EventListener
    @Transactional
    public void on(OrderPaymentExpiredEvent event) {
        update(event.orderId(), OrderSummaryJpaEntity::markPaymentExpired);
    }

    @EventListener
    @Transactional
    public void on(OrderAcceptedEvent event) {
        update(event.orderId(), summary -> summary.markAccepted(event.occurredAt()));
    }

    @EventListener
    @Transactional
    public void on(OrderRejectedEvent event) {
        update(event.orderId(), summary -> summary.markRejected(event.rejectionReason()));
    }

    @EventListener
    @Transactional
    public void on(OrderReadyForPickupEvent event) {
        update(event.orderId(), summary -> summary.markReady(event.occurredAt()));
    }

    @EventListener
    @Transactional
    public void on(OrderPickedUpEvent event) {
        update(event.orderId(), OrderSummaryJpaEntity::markPickedUp);
    }

    @EventListener
    @Transactional
    public void on(OrderDeliveredEvent event) {
        update(event.orderId(), OrderSummaryJpaEntity::markDelivered);
    }

    /**
     * A missing row means the projection was added after the order was placed.
     * It is logged rather than thrown on: refusing the event here would roll
     * back the command that produced it, so a stale read model would start
     * blocking real work.
     */
    private void update(UUID orderId, Consumer<OrderSummaryJpaEntity> change) {
        summaryRepository.findById(orderId).ifPresentOrElse(
                summary -> {
                    change.accept(summary);
                    summaryRepository.save(summary);
                },
                () -> log.warn("No order summary to update for order {}", orderId));
    }
}
