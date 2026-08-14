package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.PublishScheduledChangesUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishScheduledChangesCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PublishScheduledChangesUseCaseImpl implements PublishScheduledChangesUseCase {

    private final LoadDishPort loadDishPort;
    private final SaveDishPort saveDishPort;
    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final PublishEventPort publishEventPort;

    public PublishScheduledChangesUseCaseImpl(
            LoadDishPort loadDishPort,
            SaveDishPort saveDishPort,
            LoadRestaurantPort loadRestaurantPort,
            SaveRestaurantPort saveRestaurantPort,
            PublishEventPort publishEventPort) {
        this.loadDishPort = loadDishPort;
        this.saveDishPort = saveDishPort;
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
        this.publishEventPort = publishEventPort;
    }

    /**
     * Publishes only the dishes that are due, grouped by restaurant so the menu
     * cap is applied once per restaurant rather than once per dish.
     */
    @Override
    public void publishScheduledChanges(PublishScheduledChangesCommand command) {
        Map<RestaurantId, List<Dish>> dueByRestaurant = loadDishPort
                .loadReadyForScheduledPublish(command.dueAt())
                .stream()
                .collect(Collectors.groupingBy(Dish::getRestaurantId));

        dueByRestaurant.forEach(this::publishAll);
    }

    private void publishAll(RestaurantId restaurantId, List<Dish> dueDishes) {
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        for (Dish dish : dueDishes) {
            restaurant.getMenu().publishDish(dish.getDishId());
            dish.publishDish();
            saveDishPort.saveDish(dish);

            publishEventPort.publishAll(dish.getUncommittedEvents());
            dish.markEventAsCommitted();
        }

        saveRestaurantPort.saveRestaurant(restaurant);
        publishEventPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();
    }
}
