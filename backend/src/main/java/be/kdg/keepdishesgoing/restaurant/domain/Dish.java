package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;
import java.util.Set;

public class Dish {
    private DishId dishId;
    private RestaurantId restaurantId;

    // Published (Live) State
    private String publishedName;
    private DishType publishedType;
    private Set<FoodTag> publishedFoodTags;
    private String publishedDescription;
    private Money publishedPrice;
    private PictureUrl publishedPictureUrl;

    // Draft State (nullable - only exists when there are pending changes)
    private String draftName;
    private DishType draftType;
    private Set<FoodTag> draftFoodTags;
    private String draftDescription;
    private Money draftPrice;
    private PictureUrl draftPictureUrl;

    // Status
    private DishState status;  // DRAFT, PUBLISHED, OUT_OF_STOCK, UNPUBLISHED
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime lastPublishedAt;

    private Dish(DishId dishId, RestaurantId restaurantId,
                 String name, DishType type, Set<FoodTag> foodTags,
                 String description, Money price, PictureUrl pictureUrl) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;

        // New dish starts as draft
        this.draftName = name;
        this.draftType = type;
        this.draftFoodTags = Set.copyOf(foodTags);
        this.draftDescription = description;
        this.draftPrice = price;
        this.draftPictureUrl = pictureUrl;

        this.status = DishState.DRAFT;
    }

    public static Dish createDraft(DishId dishId, RestaurantId restaurantId,
                                   String name, DishType type, Set<FoodTag> foodTags,
                                   String description, Money price, PictureUrl pictureUrl) {
        return new Dish(dishId, restaurantId, name, type, foodTags, description, price, pictureUrl);
    }

    public void editDraft(String name, DishType type, Set<FoodTag> foodTags,
                          String description, Money price, PictureUrl pictureUrl) {
        this.draftName = name;
        this.draftType = type;
        this.draftFoodTags = Set.copyOf(foodTags);
        this.draftDescription = description;
        this.draftPrice = price;
        this.draftPictureUrl = pictureUrl;

    }

    public DishId getDishId() {
        return dishId;
    }

    public String getDraftName() {
        return draftName;
    }

    public DishType getDraftType() {
        return draftType;
    }

    public Set<FoodTag> getDraftFoodTags() {
        return draftFoodTags;
    }

    public String getDraftDescription() {
        return draftDescription;
    }

    public Money getDraftPrice() {
        return draftPrice;
    }

    public PictureUrl getDraftPictureUrl() {
        return draftPictureUrl;
    }
}
