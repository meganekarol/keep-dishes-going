package be.kdg.keepdishesgoing.restaurant.port.in.commands;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleDishChangesCommand(
        UUID restaurantId, LocalDateTime scheduledAt
) {
}
