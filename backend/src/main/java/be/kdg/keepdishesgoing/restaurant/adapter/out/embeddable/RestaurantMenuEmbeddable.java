package be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Embeddable
public class RestaurantMenuEmbeddable {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "restaurant_menu_dishes",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            schema = "kdg_restaurant"
    )
    @Column(name = "dish_id", nullable = false)
    private List<UUID> publishedDishIds = new ArrayList<>();

    protected RestaurantMenuEmbeddable() { }

    public RestaurantMenuEmbeddable(List<UUID> publishedDishIds) {
        this.publishedDishIds = publishedDishIds;
    }

    public List<UUID> getPublishedDishIds() {
        return publishedDishIds;
    }

    public void setPublishedDishIds(List<UUID> publishedDishIds) {
        this.publishedDishIds = publishedDishIds;
    }
}

