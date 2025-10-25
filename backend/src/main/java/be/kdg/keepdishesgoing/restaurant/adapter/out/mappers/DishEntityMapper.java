package be.kdg.keepdishesgoing.restaurant.adapter.out.mappers;

import be.kdg.keepdishesgoing.restaurant.adapter.out.DishJpaEntity;
import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.DishVersionEmbeddable;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishVersion;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DishEntityMapper {
    // ---------- TO DOMAIN ----------
    public Dish toDomain(DishJpaEntity entity) {
        if (entity == null) return null;

        DishVersion published = toDomainVersion(entity.getPublishedVersion());
        DishVersion draft = toDomainVersion(entity.getDraftVersion());

        return Dish.restore(
                DishId.of(entity.getDishId()),
                RestaurantId.of(entity.getRestaurantId()),
                published,
                draft,
                entity.getStatus(),
                entity.getScheduledPublishAt(),
                entity.getLastPublishedAt()
        );
    }

    private DishVersion toDomainVersion(DishVersionEmbeddable embeddable) {
        if (embeddable == null) return null;

        Set<FoodTag> foodTags = (embeddable.getFoodTags() == null || embeddable.getFoodTags().isEmpty())
                ? Set.of()
                : Arrays.stream(embeddable.getFoodTags().split(","))
                .map(String::trim)
                .map(FoodTag::valueOf)
                .collect(Collectors.toSet());

        return new DishVersion(
                embeddable.getName(),
                embeddable.getType(),
                foodTags,
                embeddable.getDescription(),
                embeddable.getPrice(),
                embeddable.getPictureUrl()
        );
    }

    // ---------- TO JPA ENTITY ----------
    public DishJpaEntity toEntity(Dish domain) {
        if (domain == null) return null;

        DishVersionEmbeddable published = toEmbeddableVersion(domain.getPublishedVersion());
        DishVersionEmbeddable draft = toEmbeddableVersion(domain.getDraftVersion());

        return new DishJpaEntity(
                domain.getDishId().uuid(),
                domain.getRestaurantId().uuid(),
                published,
                draft,
                domain.getStatus(),
                domain.getScheduledPublishAt(),
                domain.getLastPublishedAt()
        );
    }

    private DishVersionEmbeddable toEmbeddableVersion(DishVersion version) {
        if (version == null) return null;

        String foodTags = (version.foodTags() == null || version.foodTags().isEmpty())
                ? null
                : version.foodTags().stream()
                .map(FoodTag::name)
                .collect(Collectors.joining(","));

        return new DishVersionEmbeddable(
                version.name(),
                version.type(),
                foodTags,
                version.description(),
                version.price(),
                version.pictureUrl()
        );
    }
}
