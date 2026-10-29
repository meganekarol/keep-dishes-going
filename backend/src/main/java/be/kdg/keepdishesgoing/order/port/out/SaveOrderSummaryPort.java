package be.kdg.keepdishesgoing.order.port.out;

import java.util.List;

/**
 * Output port for writing the order read model in bulk.
 *
 * <p>Only a rebuild uses this. Ordinary updates come from the projection
 * listener as each event arrives.
 */
public interface SaveOrderSummaryPort {

    /**
     * Replaces the whole read model with what was derived from the event store.
     *
     * <p>Replace rather than merge: rows can be missing, and they can also be
     * out of date, and only a full rewrite fixes both.
     */
    void replaceAll(List<OrderSummaryData> summaries);
}
