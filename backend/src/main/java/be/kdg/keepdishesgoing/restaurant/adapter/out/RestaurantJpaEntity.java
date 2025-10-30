package be.kdg.keepdishesgoing.restaurant.adapter.out;

import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.AddressEmbeddable;
import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.OpeningHoursEmbeddable;
import be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable.RestaurantMenuEmbeddable;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants", schema = "kdg_restaurant")
public class RestaurantJpaEntity {

    @Id
    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Embedded
    private AddressEmbeddable address;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", nullable = false)
    private CuisineType cuisineType;

    @Column(name = "default_prep_time", nullable = false)
    private int defaultPrepTime;

    @Embedded
    private OpeningHoursEmbeddable openingHours;

    @ElementCollection
    @CollectionTable(name = "restaurant_pictures", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "picture_url")
    private List<String> pictureUrls = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RestaurantStatus status = RestaurantStatus.CLOSED;


    /** Embedded menu (policy state) */
    @Embedded
    private RestaurantMenuEmbeddable menu;

    public RestaurantJpaEntity(UUID restaurantId, UUID ownerId, String name, AddressEmbeddable address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHoursEmbeddable openingHours, List<String> pictureUrls, RestaurantStatus status, RestaurantMenuEmbeddable menu) {
        this.restaurantId = restaurantId;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.contactEmail = contactEmail;
        this.cuisineType = cuisineType;
        this.defaultPrepTime = defaultPrepTime;
        this.openingHours = openingHours;
        this.pictureUrls = pictureUrls;
        this.status = status;
        this.menu = menu;
    }

    public RestaurantJpaEntity() {
        this.restaurantId = UUID.randomUUID();
    }

    public void setId(UUID id) {
        this.restaurantId = id;
    }

    public UUID getId() {
        return restaurantId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressEmbeddable getAddress() {
        return address;
    }

    public void setAddress(AddressEmbeddable address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public CuisineType getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(CuisineType cuisineType) {
        this.cuisineType = cuisineType;
    }

    public int getDefaultPrepTime() {
        return defaultPrepTime;
    }

    public void setDefaultPrepTime(int defaultPrepTime) {
        this.defaultPrepTime = defaultPrepTime;
    }

    public OpeningHoursEmbeddable getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHoursEmbeddable openingHours) {
        this.openingHours = openingHours;
    }

    public List<String> getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(List<String> pictureUrls) {
        this.pictureUrls = pictureUrls;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public void setStatus(RestaurantStatus status) {
        this.status = status;
    }

    public RestaurantMenuEmbeddable getMenu() {
        return menu;
    }

    public void setMenu(RestaurantMenuEmbeddable menu) {
        this.menu = menu;
    }
}
