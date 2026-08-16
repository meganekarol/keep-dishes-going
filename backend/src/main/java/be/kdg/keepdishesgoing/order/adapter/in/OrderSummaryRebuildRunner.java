package be.kdg.keepdishesgoing.order.adapter.in;

import be.kdg.keepdishesgoing.order.port.in.RebuildOrderSummariesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Rebuilds the order read model once the application is up.
 *
 * <p>The projection is fed by a listener, so it only ever learns about orders
 * placed while this application was running. Anything that happened during a
 * restart, or while the listener was failing, is missing from it, and the
 * auto-rejection run reads that table to find orders nobody answered: a
 * missing row means an order that would sit pending forever.
 */
@Component
public class OrderSummaryRebuildRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderSummaryRebuildRunner.class);

    private final RebuildOrderSummariesUseCase rebuildOrderSummariesUseCase;
    private final boolean enabled;

    public OrderSummaryRebuildRunner(
            RebuildOrderSummariesUseCase rebuildOrderSummariesUseCase,
            @Value("${kdg.orders.rebuild-summaries-on-startup:true}") boolean enabled) {
        this.rebuildOrderSummariesUseCase = rebuildOrderSummariesUseCase;
        this.enabled = enabled;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildOnStartup() {
        if (!enabled) {
            log.info("Order summary rebuild on startup is switched off");
            return;
        }

        // Never fatal: a broken rebuild must not stop the application from
        // taking orders, and the table it repairs is only ever a read model.
        try {
            rebuildOrderSummariesUseCase.rebuildOrderSummaries();
        } catch (Exception e) {
            log.error("Could not rebuild the order summaries; the owner's order list may be incomplete", e);
        }
    }
}
