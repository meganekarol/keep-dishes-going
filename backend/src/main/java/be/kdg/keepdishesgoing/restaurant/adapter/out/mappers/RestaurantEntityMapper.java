package be.kdg.keepdishesgoing.restaurant.adapter.out.mappers;

import be.kdg.keepdishesgoing.restaurant.adapter.out.RestaurantJpaEntity;
import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.*;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RestaurantEntityMapper {

    /* ============================================================
       DOMAIN → JPA
       ============================================================ */
    public RestaurantJpaEntity toJpa(Restaurant domain) {
        if (domain == null) return null;

        RestaurantJpaEntity entity = new RestaurantJpaEntity();
        entity.setRestaurantId(domain.getRestaurantId().uuid());
        entity.setOwnerId(domain.getOwnerId().uuid());
        entity.setName(domain.getName());
        entity.setContactEmail(domain.getContactEmail());
        entity.setCuisineType(domain.getCuisineType());
        entity.setDefaultPrepTime(domain.getDefaultPrepTime());
        entity.setAddress(toAddressEmbeddable(domain.getAddress()));
        entity.setOpeningHours(toOpeningHoursEmbeddable(domain.getOpeningHours()));
        entity.setPictureUrls(domain.getPictureUrls());
        entity.setStatus(domain.getStatus());

        // ✅ Map RestaurantMenu to embeddable
        if (domain.getMenu() != null) {
            entity.setMenu(toRestaurantMenuEmbeddable(domain.getMenu()));
        } else {
            entity.setMenu(new RestaurantMenuEmbeddable(List.of()));
        }

        return entity;
    }

    private AddressEmbeddable toAddressEmbeddable(Address address) {
        if (address == null) return null;
        AddressEmbeddable emb = new AddressEmbeddable();
        emb.setStreet(address.street());
        emb.setNumber(address.number());
        emb.setPostalCode(address.postalCode());
        emb.setCity(address.city());
        emb.setCountry(address.country());
        return emb;
    }

    private OpeningHoursEmbeddable toOpeningHoursEmbeddable(OpeningHours openingHours) {
        if (openingHours == null) return null;
        Map<String, TimeRangeEmbeddable> scheduleEmbeddable = openingHours.schedule().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> new TimeRangeEmbeddable(entry.getValue().open(), entry.getValue().closed())
                ));
        return new OpeningHoursEmbeddable(scheduleEmbeddable);
    }

    // ✅ New: map RestaurantMenu → Embeddable
    private RestaurantMenuEmbeddable toRestaurantMenuEmbeddable(RestaurantMenu menu) {
        return new RestaurantMenuEmbeddable(
                menu.getPublishedDishes().stream()
                        .map(DishId::uuid)
                        .collect(Collectors.toList())
        );
    }

    /* ============================================================
       JPA → DOMAIN
       ============================================================ */
    public Restaurant toDomain(RestaurantJpaEntity entity) {
        if (entity == null) return null;

        RestaurantId restaurantId = new RestaurantId(entity.getRestaurantId());
        OwnerId ownerId = new OwnerId(entity.getOwnerId());
        Address address = toAddress(entity.getAddress());
        OpeningHours openingHours = toOpeningHours(entity.getOpeningHours());

        Restaurant restaurant = new Restaurant(
                restaurantId,
                ownerId,
                entity.getName(),
                address,
                entity.getContactEmail(),
                entity.getCuisineType(),
                entity.getDefaultPrepTime(),
                openingHours,
                entity.getPictureUrls()
        ).withStatus(entity.getStatus());

        return restaurant;
    }

    private Address toAddress(AddressEmbeddable emb) {
        if (emb == null) return null;
        return new Address(
                emb.getStreet(),
                emb.getNumber(),
                emb.getPostalCode(),
                emb.getCity(),
                emb.getCountry()
        );
    }

    private OpeningHours toOpeningHours(OpeningHoursEmbeddable emb) {
        if (emb == null || emb.getSchedule() == null) {
            return new OpeningHours(Map.of());
        }

        Map<DayOfWeek, TimeRange> schedule = emb.getSchedule().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> DayOfWeek.valueOf(entry.getKey()),
                        entry -> new TimeRange(entry.getValue().getOpenTime(), entry.getValue().getCloseTime())
                ));
        return new OpeningHours(schedule);
    }

    // ✅ New: map RestaurantMenuEmbeddable → Domain
    private RestaurantMenu toRestaurantMenu(RestaurantMenuEmbeddable emb, RestaurantId restaurantId) {
        return new RestaurantMenu(
                restaurantId,
                emb.getPublishedDishIds().stream()
                        .map(DishId::of)
                        .collect(Collectors.toList())
        );
    }
}
