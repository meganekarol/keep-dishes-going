package be.kdg.keepdishesgoing.order.port.in;

/**
 * Rebuilds the order read model from the event store.
 *
 * <p>The read model is kept up to date by a listener, so it only holds what it
 * heard while the application was running. Anything it missed, or got wrong,
 * stays wrong until the table is derived again from the events, which are the
 * record of what actually happened.
 */
public interface RebuildOrderSummariesUseCase {

    /** @return how many orders were written */
    int rebuildOrderSummaries();
}
