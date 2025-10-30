package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.EventPublisherHelper;
import be.kdg.keepdishesgoing.restaurant.domain.events.*;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Restaurant {
    private RestaurantId restaurantId;
    private OwnerId ownerId;
    private RestaurantMenu menu;
    private String name;
    private Address address;
    private String contactEmail;
    private CuisineType cuisineType;
    private int defaultPrepTime;
    private OpeningHours openingHours;
    private List<String> pictureUrls;

    private RestaurantStatus status = RestaurantStatus.CLOSED;

    // Composition: event publisher
    private final EventPublisherHelper eventPublisher = new EventPublisherHelper();

    public Restaurant(RestaurantId restaurantId,OwnerId ownerId, String name, Address address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHours openingHours, List<String> pictureUrls) {
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


    public static Restaurant createRestaurant(RestaurantId restaurantId, OwnerId ownerId, String name, Address address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHours openingHours, List<String> pictureUrls) {
        Restaurant restaurant =  new Restaurant(
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

        // Determine initial status using the domain logic itself
        LocalDateTime now = LocalDateTime.now();
        restaurant.status = restaurant.isOpen(now)
                ? RestaurantStatus.OPEN
                : RestaurantStatus.CLOSED;

        restaurant.eventPublisher.register(new RestaurantCreatedEvent(
                restaurantId.uuid(),
                ownerId.uuid(),
                name,
                cuisineType.toString()
        ));

        return restaurant;
    }

    public Restaurant withStatus(RestaurantStatus status) {
        this.status = status;
        return this;
    }

    /** Define or update the weekly schedule */
    public void defineOpeningHours(Map<DayOfWeek, TimeRange> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            throw new IllegalArgumentException("Opening hours cannot be empty");
        }
        this.openingHours = new OpeningHours(schedule);
    }

    /** Manually open the restaurant (overrides schedule) */
    public void manuallyOpen() {
        if (status == RestaurantStatus.MANUALLY_OPENED) return;
        this.status = RestaurantStatus.MANUALLY_OPENED;
        eventPublisher.register(new RestaurantManuallyOpenedEvent(this.restaurantId.uuid()));
    }

    /** Manually close the restaurant (overrides schedule) */
    public void manuallyClose() {
        if (status == RestaurantStatus.MANUALLY_CLOSED) return;
        this.status = RestaurantStatus.MANUALLY_CLOSED;
        eventPublisher.register(new RestaurantManuallyClosedEvent(this.restaurantId.uuid()));
    }

    /** Evaluate whether the restaurant is open based on schedule and manual overrides */
    public boolean isOpen(LocalDateTime now) {
        if (status == RestaurantStatus.MANUALLY_CLOSED) return false;
        if (status == RestaurantStatus.MANUALLY_OPENED) return true;
        return openingHours.isOpenAt(now.toLocalTime(), now.getDayOfWeek());
    }

    // Event access methods
    // Event access methods
    public List<DomainEvent> getUncommittedEvents() {
        return eventPublisher.getEvents();
    }

    public void markEventAsCommitted() {
        eventPublisher.clear();
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

    public List<String> getPictureUrls() {
        return pictureUrls;
    }

    public RestaurantStatus getStatus() {
        return status;
    }

    public RestaurantMenu getMenu() {
        return menu;
    }
}
