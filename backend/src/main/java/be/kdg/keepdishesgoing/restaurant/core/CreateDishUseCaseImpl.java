package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantEventPublisherPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveDishPort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CreateDishUseCaseImpl implements CreateDishUseCase {

    private final LoadRestaurantPort restaurantPort;
    private final SaveDishPort dishPort;
    private final RestaurantEventPublisherPort restaurantEventPublisherPort;

    public CreateDishUseCaseImpl(LoadRestaurantPort restaurantPort, SaveDishPort dishPort, RestaurantEventPublisherPort restaurantEventPublisherPort) {
        this.restaurantPort = restaurantPort;
        this.dishPort = dishPort;
        this.restaurantEventPublisherPort = restaurantEventPublisherPort;
    }

    @Override
    public Dish createDish(CreateDishCommand command) {
        Restaurant restaurant = restaurantPort
                .loadById(RestaurantId.of(command.restaurantId()))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        DishVersion draftVersion = new DishVersion(
                command.name(),
                command.type(),
                command.foodTags(),
                command.description(),
                command.price(),
                command.pictureUrl());

        Dish dish = Dish.createDraft(
                DishId.create(),
                restaurant.getRestaurantId(),
                draftVersion
        );

        dishPort.saveDish(dish);

        restaurantEventPublisherPort.publishAll(dish.getUncommittedEvents());
        restaurantEventPublisherPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        dish.markEventAsCommitted();
        restaurant.markEventAsCommitted();

        return dish;
    }
}
