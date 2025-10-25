package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.ApplyPendingChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ApplyPendingChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class ScheduledDishPublisher {

    private final ApplyPendingChangesUseCase applyPendingChangesUseCase;
    private final LoadDishPort loadDishPort;

    public ScheduledDishPublisher(ApplyPendingChangesUseCase applyPendingChangesUseCase, LoadDishPort loadDishPort) {
        this.applyPendingChangesUseCase = applyPendingChangesUseCase;
        this.loadDishPort = loadDishPort;
    }

    @Scheduled(fixedRate = 60000)
    public void applyScheduledChanges() {
        LocalDateTime now = LocalDateTime.now();

        loadDishPort.loadReadyForScheduledPublish(now)
                .stream()
                .collect(Collectors.groupingBy(Dish::getRestaurantId))
                .forEach((restaurantId, dishes) -> {
                    ApplyPendingChangesCommand command =
                            new ApplyPendingChangesCommand(restaurantId.uuid());
                    applyPendingChangesUseCase.applyPendingChanges(command);
                });
    }
}
