package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Money;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.PictureUrl;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class CreateDishUseCaseImpl implements CreateDishUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public CreateDishUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public Dish createDish(CreateDishCommand command) {
        Restaurant restaurant = restaurantRepositoryPort
                .findById(RestaurantId.of(command.restaurantId()))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
        Dish dish = restaurant.addDish(
                command.name(),
                command.type(),
                command.foodTags(),
                command.description(),
                Money.of(command.price()),
                new PictureUrl(command.pictureUrl())
        );
        restaurantRepositoryPort.saveRestaurant(restaurant);
        return dish;
    }
}
