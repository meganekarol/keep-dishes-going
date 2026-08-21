package be.kdg.keepdishesgoing.order.adapter.in.scheduling;

import be.kdg.keepdishesgoing.order.port.in.ExpireAbandonedCheckoutsUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.ExpireAbandonedCheckoutsCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * The clock as an inbound adapter: it decides when, the use case decides what.
 */
@Component
public class AbandonedCheckoutScheduler {

    /** Longer than the provider's own checkout window, so nobody mid-payment is swept. */
    private static final int PAYMENT_WINDOW_MINUTES = 35;

    private final ExpireAbandonedCheckoutsUseCase expireAbandonedCheckoutsUseCase;

    public AbandonedCheckoutScheduler(ExpireAbandonedCheckoutsUseCase expireAbandonedCheckoutsUseCase) {
        this.expireAbandonedCheckoutsUseCase = expireAbandonedCheckoutsUseCase;
    }

    @Scheduled(fixedRate = 300000)
    public void expireAbandonedCheckouts() {
        expireAbandonedCheckoutsUseCase.expireAbandonedCheckouts(new ExpireAbandonedCheckoutsCommand(
                LocalDateTime.now().minusMinutes(PAYMENT_WINDOW_MINUTES)));
    }
}
