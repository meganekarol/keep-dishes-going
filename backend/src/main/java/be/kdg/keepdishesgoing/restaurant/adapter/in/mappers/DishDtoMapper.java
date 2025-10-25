package be.kdg.keepdishesgoing.restaurant.adapter.in.mappers;

import org.springframework.stereotype.Component;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.EditDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateDishResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.EditDishResponse;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.EditDishAsDraftCommand;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DishDtoMapper {
    /* ---------- DTO → Command ---------- */

    public CreateDishCommand toCreateCommand(UUID restaurantId, CreateDishRequest request) {
        Set<FoodTag> foodTags = request.foodTags().stream()
                .map(FoodTag::valueOf)
                .collect(Collectors.toSet());

        return new CreateDishCommand(
                restaurantId,
                request.name(),
                request.type(),
                foodTags,
                request.description(),
                request.price(),
                request.pictureUrl()
        );
    }

    public EditDishAsDraftCommand toEditCommand(UUID restaurantId, UUID dishId, EditDishRequest request) {
        return new EditDishAsDraftCommand(
                restaurantId,
                dishId,
                request.name(),
                request.type(),
                request.foodTags(),
                request.description(),
                request.price(),
                request.pictureUrl()
        );
    }

    /* ---------- Domain → DTO ---------- */

    public CreateDishResponse toCreateResponse(Dish dish) {
        var draft = dish.getDraftVersion();
        return new CreateDishResponse(
                dish.getRestaurantId().uuid(),
                dish.getDishId().uuid(),
                draft.name(),
                draft.type(),
                draft.foodTags(),
                draft.description(),
                draft.price(),
                draft.pictureUrl()
        );
    }

    public EditDishResponse toEditResponse(Dish dishDraft) {
        var draft = dishDraft.getDraftVersion();
        return new EditDishResponse(
                draft.name(),
                draft.type(),
                draft.foodTags(),
                draft.description(),
                draft.price(),
                draft.pictureUrl()
        );
    }
}
