package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import java.time.LocalDateTime;

/**
 * @param dueAt every dish scheduled at or before this moment goes live
 */
public record PublishScheduledChangesCommand(
        LocalDateTime dueAt
) {
}
