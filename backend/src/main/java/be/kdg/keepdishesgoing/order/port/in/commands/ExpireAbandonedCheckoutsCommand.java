package be.kdg.keepdishesgoing.order.port.in.commands;

import java.time.LocalDateTime;

/**
 * Abandon every checkout still unpaid that was started before {@code cutoff}.
 *
 * @param cutoff the moment the payment window closes; the caller owns the
 *               clock, so this stays testable without waiting it out
 */
public record ExpireAbandonedCheckoutsCommand(LocalDateTime cutoff) {
}
