package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Pure domain model - no framework dependencies
public class Restaurant {
    private RestaurantId restaurantId;
    private OwnerId ownerId;
    private String name;
    private Address address;
    private String contactEmail;
    private CuisineType cuisineType;
    private int defaultPrepTime;
    private OpeningHours openingHours;
    private List<PictureUrl> pictureUrls;
    private List<Dish> dishes = new ArrayList<>();


    public Restaurant(RestaurantId restaurantId,OwnerId ownerId, String name, Address address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHours openingHours, List<PictureUrl> pictureUrls) {
        this.restaurantId = restaurantId;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.contactEmail = contactEmail;
        this.cuisineType = cuisineType;
        this.defaultPrepTime = defaultPrepTime;
        this.openingHours = openingHours;
        this.pictureUrls = pictureUrls;
    }

    public static Restaurant createRestaurant(RestaurantId restaurantId, OwnerId ownerId, String name, Address address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHours openingHours, List<PictureUrl> pictureUrls) {
        return new Restaurant(
                restaurantId,
                ownerId,
                name,
                address,
                contactEmail,
                cuisineType,
                defaultPrepTime,
                openingHours,
                pictureUrls
        );
    }

    public Dish addDish(DishVersion dishDraft) {
        Dish newDish = Dish.createDraft(
                DishId.create(),
                this.restaurantId,
                dishDraft
        );
        dishes.add(newDish);
        return newDish;
    }

    public void editDishDraft(DishId dishId,DishVersion dishDraft) {
        Dish dish = findDish(dishId);
        dish.editDraft(dishDraft);
    }

    public void publishDish(DishId dishId) {
        Dish dish = findDish(dishId);

        if (!dish.hasDraft() && dish.getStatus() != DishState.UNPUBLISHED) {
            throw new IllegalStateException("No draft changes available to publish");
        }
        int dishCount = countPublishedDishes();
        if (dishCount >= 10) {
            throw new RuntimeException("Cannot publish more than 10 dishes");
        }
        dish.publishDish();
    }

    public void unpublishDish(DishId dishId) {
        Dish dish = findDish(dishId);

        if (dish.getStatus() != DishState.PUBLISHED) {
            throw new IllegalStateException("Dish is not published");
        }
        dish.unpublishDish();
    }


    // Publish multiple dishes at once
    public void publishAllPendingChanges() {
        List<Dish> pendingDishes = dishes.stream()
                .filter(Dish::hasPendingChanges)
                .toList();

        // Check business rule
        int totalAfterPublish = countPublishedDishes() +
                (int) pendingDishes.stream()
                        .filter(d -> d.getStatus() == DishState.UNPUBLISHED)
                        .count();

        if (totalAfterPublish > 10) {
            throw new IllegalArgumentException("Cannot publish more than 10 dishes in total");
        }

        pendingDishes.forEach(Dish::publishDish);
    }

    public void scheduleChanges(LocalDateTime scheduledTime) {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule changes in the past");
        }
        dishes.forEach(dish -> {
            if (dish.hasPendingChanges()) {
                dish.scheduleChanges(scheduledTime);
            }
        });
    }
    public void markDishOutOfStock(DishId dishId) {
      findDish(dishId).markDishOutOfStock();
    }

    public void markDishInStock(DishId dishId) {
      findDish(dishId).markDishInStock();
    }


    public Dish findDish(DishId dishId) {
        return dishes.stream()
                .filter(d -> d.getDishId().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Dish not found: " + dishId));
    }

    public int countPublishedDishes() {
      return (int) dishes.stream()
              .filter(d -> d.getStatus() == DishState.PUBLISHED)
              .count();
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public OwnerId getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public int getDefaultPrepTime() {
        return defaultPrepTime;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public List<PictureUrl> getPictureUrls() {
        return pictureUrls;
    }

    public List<Dish> getDishes() {
        return dishes;
    }
}
