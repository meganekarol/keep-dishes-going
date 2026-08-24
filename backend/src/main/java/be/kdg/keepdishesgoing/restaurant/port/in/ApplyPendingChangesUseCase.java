package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ApplyPendingChangesCommand;

/** Lets an owner put every pending dish change live at once (US-06). */
public interface ApplyPendingChangesUseCase {
    /**
     * Publishes every dish that has changes waiting.
     *
     * @param command which restaurant to apply the changes for
     */
    void applyPendingChanges(ApplyPendingChangesCommand command);
}
