package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.adapter.out.mappers.RestaurantMenuEntityMapper;
import be.kdg.keepdishesgoing.restaurant.adapter.out.repositories.RestaurantMenuJpaRepository;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantMenuPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateRestaurantMenuPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RestaurantMenuJpaAdapter implements LoadRestaurantMenuPort, UpdateRestaurantMenuPort {

    private final RestaurantMenuJpaRepository restaurantMenus;
    private final RestaurantMenuEntityMapper mapper;

    public RestaurantMenuJpaAdapter(RestaurantMenuJpaRepository menus, RestaurantMenuEntityMapper mapper) {
        this.restaurantMenus = menus;
        this.mapper = mapper;
    }

    @Override
    public Optional<RestaurantMenu> loadByRestaurantId(RestaurantId restaurantId) {
        return this.restaurantMenus.findByRestaurantId(restaurantId.uuid())
                .map(mapper::toDomain);
    }

    @Override
    public RestaurantMenu updateRestaurantMenu(RestaurantMenu restaurantMenu) {
        RestaurantMenuJpaEntity entity = mapper.toJpa(restaurantMenu);
        RestaurantMenuJpaEntity saved = this.restaurantMenus.save(entity);
        return mapper.toDomain(saved);
    }
}
