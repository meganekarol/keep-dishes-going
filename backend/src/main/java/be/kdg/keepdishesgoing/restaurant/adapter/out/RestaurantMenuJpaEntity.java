package be.kdg.keepdishesgoing.restaurant.adapter.out;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "restaurant_menus")
public class RestaurantMenuJpaEntity {

    @Id
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "menu_published_dishes",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Column(name = "dish_id", nullable = false)
    private List<UUID> publishedDishIds = new ArrayList<>();

    public RestaurantMenuJpaEntity() {
    }

    public RestaurantMenuJpaEntity(UUID restaurantId, List<UUID> publishedDishIds) {
        this.restaurantId = restaurantId;
        this.publishedDishIds = publishedDishIds;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<UUID> getPublishedDishIds() {
        return publishedDishIds;
    }

    public void setPublishedDishIds(List<UUID> publishedDishIds) {
        this.publishedDishIds = publishedDishIds;
    }
}
