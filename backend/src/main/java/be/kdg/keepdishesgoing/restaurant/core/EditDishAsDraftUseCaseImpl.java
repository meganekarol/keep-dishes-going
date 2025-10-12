package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Money;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.PictureUrl;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.EditDishAsDraftCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class EditDishAsDraftUseCaseImpl implements EditDishAsDraftUseCase {

    private final RestaurantRepositoryPort restaurantRepositoryPort;

    public EditDishAsDraftUseCaseImpl(RestaurantRepositoryPort restaurantRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
    }

    @Override
    public Dish editDish(EditDishAsDraftCommand command) {
        RestaurantId restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = restaurantRepositoryPort.findById(restaurantId).
                orElseThrow(() -> new RuntimeException("Restaurant not found"));

        DishId dishId = DishId.of(command.dishId());
        Money price = Money.of(command.price());
        PictureUrl picture = new PictureUrl(command.pictureUrl());
        restaurant.editDishDraft(
                dishId,
                command.name(),
                command.type(),
                command.foodTags(),
                command.description(),
                price,
                picture
        );

        restaurantRepositoryPort.saveRestaurant(restaurant);

        return restaurant.findDish(dishId);
    }
}
