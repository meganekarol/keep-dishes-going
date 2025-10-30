package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.DishVersionEmbeddable;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dishes", schema = "kdg_restaurant")
public class DishJpaEntity {

    @Id
    @Column(name = "dish_id", nullable = false)
    private UUID dishId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "published_name")),
            @AttributeOverride(name = "type", column = @Column(name = "published_type")),
            @AttributeOverride(name = "foodTags", column = @Column(name = "published_food_tags")),
            @AttributeOverride(name = "description", column = @Column(name = "published_description")),
            @AttributeOverride(name = "price", column = @Column(name = "published_price_amount")),
            @AttributeOverride(name = "pictureUrl", column = @Column(name = "published_picture_url"))
    })
    private DishVersionEmbeddable publishedVersion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "draft_name")),
            @AttributeOverride(name = "type", column = @Column(name = "draft_type")),
            @AttributeOverride(name = "foodTags", column = @Column(name = "draft_food_tags")),
            @AttributeOverride(name = "description", column = @Column(name = "draft_description")),
            @AttributeOverride(name = "price", column = @Column(name = "draft_price_amount")),
            @AttributeOverride(name = "pictureUrl", column = @Column(name = "draft_picture_url"))
    })
    private DishVersionEmbeddable draftVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DishState status;

    @Column(name = "scheduled_publish_at")
    private LocalDateTime scheduledPublishAt;

    @Column(name = "last_published_at")
    private LocalDateTime lastPublishedAt;


    public DishJpaEntity() {

    }

    public DishJpaEntity(UUID dishId, UUID restaurantId, DishVersionEmbeddable publishedVersion, DishVersionEmbeddable draftVersion, DishState status, LocalDateTime scheduledPublishAt, LocalDateTime lastPublishedAt) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;
        this.publishedVersion = publishedVersion;
        this.draftVersion = draftVersion;
        this.status = status;
        this.scheduledPublishAt = scheduledPublishAt;
        this.lastPublishedAt = lastPublishedAt;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }


    public DishVersionEmbeddable getPublishedVersion() {
        return publishedVersion;
    }

    public void setPublishedVersion(DishVersionEmbeddable publishedVersion) {
        this.publishedVersion = publishedVersion;
    }

    public DishVersionEmbeddable getDraftVersion() {
        return draftVersion;
    }

    public void setDraftVersion(DishVersionEmbeddable draftVersion) {
        this.draftVersion = draftVersion;
    }

    public DishState getStatus() {
        return status;
    }

    public void setStatus(DishState status) {
        this.status = status;
    }

    public LocalDateTime getScheduledPublishAt() {
        return scheduledPublishAt;
    }

    public void setScheduledPublishAt(LocalDateTime scheduledPublishAt) {
        this.scheduledPublishAt = scheduledPublishAt;
    }

    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }

    public void setLastPublishedAt(LocalDateTime lastPublishedAt) {
        this.lastPublishedAt = lastPublishedAt;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }
}
