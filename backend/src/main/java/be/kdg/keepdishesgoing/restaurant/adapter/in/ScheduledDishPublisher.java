package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.port.in.PublishScheduledChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishScheduledChangesCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * The clock as an inbound adapter: it decides when, the use case decides what.
 *
 * <p>Reading the due dishes here and handing the whole restaurant to the
 * apply-everything use case published drafts that were not due yet, and drafts
 * the owner had never scheduled at all.
 */
@Component
public class ScheduledDishPublisher {

    private final PublishScheduledChangesUseCase publishScheduledChangesUseCase;

    public ScheduledDishPublisher(PublishScheduledChangesUseCase publishScheduledChangesUseCase) {
        this.publishScheduledChangesUseCase = publishScheduledChangesUseCase;
    }

    @Scheduled(fixedRate = 60000)
    public void applyScheduledChanges() {
        publishScheduledChangesUseCase.publishScheduledChanges(
                new PublishScheduledChangesCommand(LocalDateTime.now()));
    }
}
