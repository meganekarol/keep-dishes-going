package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.MarkOrderReadyCommand;

/** Lets a restaurant say the food is cooked and waiting (US-12). */
public interface MarkOrderReadyUseCase {
    /**
     * Marks the order ready for a courier to collect.
     *
     * @param command which order is ready
     */
    void markOrderReady(MarkOrderReadyCommand command);
}
