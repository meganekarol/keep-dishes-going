package be.kdg.keepdishesgoing.order.adapter.in.scheduling;

import be.kdg.keepdishesgoing.order.port.in.RejectStaleOrdersUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.RejectStaleOrdersCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * The clock as an inbound adapter: it decides when, the use case decides what.
 *
 * <p>Reading the event store here meant replaying every order ever placed once
 * a minute, forever, to find the handful still waiting. The use case reads the
 * order summaries instead, which already know which ones those are.
 */
@Component
public class OrderAutoRejectionScheduler {

    private static final int ACCEPTANCE_TIMEOUT_MINUTES = 5;

    private final RejectStaleOrdersUseCase rejectStaleOrdersUseCase;

    public OrderAutoRejectionScheduler(RejectStaleOrdersUseCase rejectStaleOrdersUseCase) {
        this.rejectStaleOrdersUseCase = rejectStaleOrdersUseCase;
    }

    @Scheduled(fixedRate = 60000)
    public void rejectUnansweredOrders() {
        rejectStaleOrdersUseCase.rejectStaleOrders(new RejectStaleOrdersCommand(
                LocalDateTime.now().minusMinutes(ACCEPTANCE_TIMEOUT_MINUTES)));
    }
}
