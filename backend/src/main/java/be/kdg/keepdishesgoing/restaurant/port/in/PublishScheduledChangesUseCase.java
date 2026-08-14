package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishScheduledChangesCommand;

/**
 * Publishes the dishes whose scheduled moment has arrived (US-07).
 *
 * <p>Separate from {@link ApplyPendingChangesUseCase}, which takes every draft
 * a restaurant holds. Here only the dishes that are actually due go live.
 */
public interface PublishScheduledChangesUseCase {
    void publishScheduledChanges(PublishScheduledChangesCommand command);
}
