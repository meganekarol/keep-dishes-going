package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.adapter.out.mappers.DishEntityMapper;
import be.kdg.keepdishesgoing.restaurant.adapter.out.repositories.DishJpaRepository;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveDishPort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DishJpaAdapter implements LoadDishPort, SaveDishPort {

    private final DishJpaRepository dishes;
    private final DishEntityMapper mapper;

    public DishJpaAdapter(DishJpaRepository dishes, DishEntityMapper mapper) {
        this.dishes = dishes;
        this.mapper = mapper;
    }

    @Override
    public Optional<Dish> loadById(DishId dish) {
        return this.dishes.findById(dish.uuid())
                .map(mapper::toDomain);
    }

    @Override
    public List<Dish> loadAll() {
        return this.dishes.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Dish> loadPendingDishesByRestaurant(RestaurantId restaurant) {
        return this.dishes.findPendingChangesByRestaurant(restaurant.uuid()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Dish> loadReadyForScheduledPublish(LocalDateTime time) {
        return this.dishes.findReadyForScheduledPublish(time).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Dish saveDish(Dish dish) {
        DishJpaEntity entity = mapper.toEntity(dish);
        DishJpaEntity saved = this.dishes.save(entity);
        return mapper.toDomain(saved);
    }
}
