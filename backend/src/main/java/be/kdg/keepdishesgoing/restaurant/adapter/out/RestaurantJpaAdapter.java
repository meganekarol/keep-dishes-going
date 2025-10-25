package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.adapter.out.mappers.RestaurantEntityMapper;
import be.kdg.keepdishesgoing.restaurant.adapter.out.repositories.RestaurantJpaRepository;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateRestaurantPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RestaurantJpaAdapter implements LoadRestaurantPort, UpdateRestaurantPort {

    private final RestaurantJpaRepository restaurants;
    private final RestaurantEntityMapper mapper;

    public RestaurantJpaAdapter(RestaurantJpaRepository restaurantJpaRepository, RestaurantEntityMapper mapper) {
        this.restaurants = restaurantJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Restaurant> loadByOwnerId(OwnerId owner) {
        return this.restaurants.findByOwnerId(owner.uuid())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Restaurant> loadById(RestaurantId restaurant) {
        return this.restaurants.findById(restaurant.uuid())
                .map(mapper::toDomain);
    }

    @Override
    public List<Restaurant> loadAll() {
        List<RestaurantJpaEntity> restaurants = this.restaurants.findAll();
        return restaurants.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Restaurant updateRestaurant(Restaurant restaurant) {
        RestaurantJpaEntity entity = mapper.toJpa(restaurant);
        RestaurantJpaEntity saved = this.restaurants.save(entity);
        return mapper.toDomain(saved);
    }
}
