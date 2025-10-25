package be.kdg.keepdishesgoing.restaurant.adapter.out.mappers;

import be.kdg.keepdishesgoing.restaurant.adapter.out.RestaurantMenuJpaEntity;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantMenuEntityMapper {

    public RestaurantMenuJpaEntity toJpa(RestaurantMenu menu) {
        List<UUID> dishIds = menu.getPublishedDishes()
                .stream()
                .map(DishId::uuid)
                .collect(Collectors.toList());

        return new RestaurantMenuJpaEntity(menu.getRestaurantId().uuid(), dishIds);
    }

    public RestaurantMenu toDomain(RestaurantMenuJpaEntity entity) {
        List<DishId> dishes = entity.getPublishedDishIds()
                .stream()
                .map(DishId::of)
                .collect(Collectors.toList());

        return new RestaurantMenu(RestaurantId.of(entity.getRestaurantId()), dishes);
    }
}
