package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.EventPublisherHelper;
import be.kdg.keepdishesgoing.common.events.OpeningHoursSlot;
import be.kdg.keepdishesgoing.common.events.RestaurantCreatedEvent;
import be.kdg.keepdishesgoing.common.events.RestaurantManuallyClosedEvent;
import be.kdg.keepdishesgoing.common.events.RestaurantManuallyOpenedEvent;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;
import java.util.List;

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
        // A restaurant always has a menu, empty to begin with. Without this the
        // menu policy (max 10 published dishes) has nothing to run against.
        this.menu = RestaurantMenu.forRestaurant(restaurantId);
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
                cuisineType.toString(),
                toSlots(openingHours)
        ));

        return restaurant;
    }

    /**
     * Translates the weekly schedule into the shared-kernel representation, so
     * other contexts can evaluate opening hours without reaching into this one.
     */
    private static List<OpeningHoursSlot> toSlots(OpeningHours openingHours) {
        if (openingHours == null || openingHours.schedule() == null) {
            return List.of();
        }
        return openingHours.schedule().entrySet().stream()
                .map(entry -> new OpeningHoursSlot(
                        entry.getKey(),
                        entry.getValue().open(),
                        entry.getValue().closed()
                ))
                .toList();
    }

    public Restaurant withStatus(RestaurantStatus status) {
        this.status = status;
        return this;
    }

    /** Restore the previously published dishes when rehydrating from persistence. */
    public Restaurant withMenu(RestaurantMenu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu cannot be null");
        }
        this.menu = menu;
        return this;
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
