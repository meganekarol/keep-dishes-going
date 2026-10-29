package be.kdg.keepdishesgoing.order.port.in.commands;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Where the courier carrying an order was at {@code reportedAt} (US-29).
 */
public record RecordCourierLocationCommand(UUID orderId, double lat, double lng,
                                           LocalDateTime reportedAt) {
}
