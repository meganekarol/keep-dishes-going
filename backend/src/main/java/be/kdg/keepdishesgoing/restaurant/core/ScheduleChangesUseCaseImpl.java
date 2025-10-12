package be.kdg.keepdishesgoing.restaurant.core;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ScheduleChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ScheduleDishChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class ScheduleChangesUseCaseImpl implements ScheduleChangesUseCase {

    private final RestaurantRepositoryPort restaurantRepository;

    public ScheduleChangesUseCaseImpl(RestaurantRepositoryPort restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public void schedulePendingChanges(ScheduleDishChangesCommand command) {
        Restaurant restaurant = restaurantRepository.findById(RestaurantId.of(command.restaurantId()))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + command.restaurantId()));

        restaurant.scheduleChanges(command.scheduledAt());
        restaurantRepository.saveRestaurant(restaurant);
    }
}
