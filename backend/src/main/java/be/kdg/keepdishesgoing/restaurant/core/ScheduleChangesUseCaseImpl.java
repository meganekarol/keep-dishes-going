package be.kdg.keepdishesgoing.restaurant.core;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ScheduleChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ScheduleDishChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveDishPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ScheduleChangesUseCaseImpl implements ScheduleChangesUseCase {

    private final LoadDishPort loadDishPort;
    private final SaveDishPort saveDishPort;

    public ScheduleChangesUseCaseImpl(LoadDishPort loadDishPort, SaveDishPort saveDishPort) {
        this.loadDishPort = loadDishPort;
        this.saveDishPort = saveDishPort;
    }

    @Override
    public void schedulePendingChanges(ScheduleDishChangesCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        LocalDateTime scheduledTime = command.scheduledAt();

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule changes in the past.");
        }
        List<Dish> pendingDishes = loadDishPort.loadPendingDishesByRestaurant(restaurantId);
        if (pendingDishes.isEmpty()) {
            return;
        }
        for (Dish pendingDish : pendingDishes) {
            pendingDish.scheduleChanges(scheduledTime);
            saveDishPort.saveDish(pendingDish);
        }
    }

}
