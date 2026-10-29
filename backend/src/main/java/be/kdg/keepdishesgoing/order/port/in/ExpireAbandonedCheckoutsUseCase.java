package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.ExpireAbandonedCheckoutsCommand;

/**
 * Clears out checkouts nobody ever paid for (US-23).
 *
 * <p>Most abandoned checkouts are reported by the provider, whose page times
 * out on its own. This sweeps up the rest: a customer who closed the tab before
 * the provider noticed leaves an order that would otherwise sit unpaid forever.
 */
public interface ExpireAbandonedCheckoutsUseCase {

    /** @return how many orders were abandoned */
    int expireAbandonedCheckouts(ExpireAbandonedCheckoutsCommand command);
}
