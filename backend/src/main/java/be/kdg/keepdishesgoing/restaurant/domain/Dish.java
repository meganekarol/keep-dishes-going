package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;

public class Dish {
    private DishId dishId;
    private RestaurantId restaurantId;

    private DishVersion publishedVersion; // Published (Live) State
    private DishVersion draftVersion;     // Draft State (nullable - only exists when there are pending changes)
    private DishState status;  // DRAFT, PUBLISHED, OUT_OF_STOCK, UNPUBLISHED
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime lastPublishedAt;

    private Dish(DishId dishId, RestaurantId restaurantId, DishVersion draftVersion) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;

        // New dish starts as draft
        this.draftVersion = draftVersion;
        this.status = DishState.DRAFT;
    }

    public static Dish restore(
            DishId dishId,
            RestaurantId restaurantId,
            DishVersion publishedVersion,
            DishVersion draftVersion,
            DishState status,
            LocalDateTime scheduledPublishAt,
            LocalDateTime lastPublishedAt
    ) {
        Dish dish = new Dish(dishId, restaurantId, draftVersion);
        dish.publishedVersion = publishedVersion;
        dish.status = status;
        dish.scheduledPublishAt = scheduledPublishAt;
        dish.lastPublishedAt = lastPublishedAt;
        return dish;
    }


    public static Dish createDraft(DishId dishId, RestaurantId restaurantId, DishVersion draftVersion) {
        return new Dish(dishId, restaurantId, draftVersion);
    }

    public void editDraft(DishVersion draftVersion) {
        this.draftVersion = draftVersion;
    }

    public void publishDish()  {
        // Ensure that a draft version actually exists before trying to publish it
        if (!hasDraft()) {
            throw new IllegalStateException("No draft version to publish");
        }
        this.publishedVersion = draftVersion;
        this.draftVersion = null;
        this.status = DishState.PUBLISHED;
        this.lastPublishedAt = LocalDateTime.now();
        this.scheduledPublishAt = null;
    }

    public void unpublishDish() {
        if (hasDraft()) {
            throw new IllegalStateException("Cannot unpublish a draft dish");
        }
        this.status = DishState.UNPUBLISHED;
    }

    public void markDishOutOfStock() {
        if (!isPublished()) {
            throw new IllegalArgumentException(
                    "Only published dishes can be marked out of stock. Current status: " + status
            );
        }
        this.status = DishState.OUT_OF_STOCK;
    }

    public void markDishInStock() {
        this.status = DishState.PUBLISHED;
    }

    public void scheduleChanges(LocalDateTime dateTime) {
        if (!hasPendingChanges()) {
            throw new IllegalStateException("No pending changes to schedule.");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule publishing in the past.");
        }

        this.scheduledPublishAt = dateTime;
    }

    // --- State Checks ---
    public boolean hasPendingChanges() {
        return draftVersion != null && !draftVersion.equals(publishedVersion);
    }

    public boolean hasDraft() {
        return draftVersion != null;
    }

    public boolean isPublished() {
        return status == DishState.PUBLISHED;
    }

    // -- Getters ---
    public DishId getDishId() {
        return dishId;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public DishVersion getPublishedVersion() {
        return publishedVersion;
    }

    public DishVersion getDraftVersion() {
        return draftVersion;
    }

    public DishState getStatus() {
        return status;
    }

    public LocalDateTime getScheduledPublishAt() {
        return scheduledPublishAt;
    }

    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }
}
