package be.kdg.keepdishesgoing.order.port.in.commands;

import java.time.LocalDateTime;

/**
 * Reject every order still unanswered that was placed before {@code cutoff}.
 *
 * @param cutoff the moment the acceptance window closes; the caller owns the
 *               clock, so this stays testable without waiting five minutes
 */
public record RejectStaleOrdersCommand(LocalDateTime cutoff) {
}
