package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.EditDishAsDraftCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateDishPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EditDishAsDraftUseCaseImpl implements EditDishAsDraftUseCase {
    private final UpdateDishPort updateDishPort;
    private final LoadDishPort loadDishPort;

    public EditDishAsDraftUseCaseImpl(UpdateDishPort updateDishPort, LoadDishPort loadDishPort) {
        this.updateDishPort = updateDishPort;
        this.loadDishPort = loadDishPort;
    }

    @Override
    public Dish editDish(EditDishAsDraftCommand command) {

        DishId dishId = DishId.of(command.dishId());
        Dish dish = loadDishPort.loadById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Dish not found"));

        DishVersion newDraft = new DishVersion(
                command.name(),
                command.type(),
                command.foodTags(),
                command.description(),
                command.price(),
                command.pictureUrl()
        );

        // Apply the change through domain logic
        dish.editDraft(newDraft);

        // Persist the modified dish
        updateDishPort.updateDish(dish);

        // Return updated dish
        return dish;
    }
}
