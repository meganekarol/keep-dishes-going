package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.EventPublisherHelper;
import be.kdg.keepdishesgoing.common.events.OpeningHoursSlot;
import be.kdg.keepdishesgoing.common.events.RestaurantCreatedEvent;
import be.kdg.keepdishesgoing.common.events.RestaurantManuallyClosedEvent;
import be.kdg.keepdishesgoing.common.events.RestaurantManuallyOpenedEvent;
import be.kdg.keepdishesgoing.common.events.RestaurantScheduleResumedEvent;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A restaurant on the platform (US-02).
 *
 * <p>Owns its menu, its opening hours, and whether it is currently taking orders.
 */
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

    private final EventPublisherHelper eventPublisher = new EventPublisherHelper();

    /**
     * Builds a restaurant with an empty menu.
     *
     * @param restaurantId    the restaurant's identifier
     * @param ownerId         the owner it belongs to
     * @param name            the restaurant's name
     * @param address         where it is
     * @param contactEmail    the address customers can reach it on
     * @param cuisineType     the kind of food it serves
     * @param defaultPrepTime how many minutes it usually needs to cook an order
     * @param openingHours    when it is open
     * @param pictureUrls     pictures of the restaurant
     */
    public Restaurant(RestaurantId restaurantId, OwnerId ownerId, String name, Address address, String contactEmail, CuisineType cuisineType, int defaultPrepTime, OpeningHours openingHours, List<String> pictureUrls) {
        this.restaurantId = restaurantId;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.contactEmail = contactEmail;
        this.cuisineType = cuisineType;
        this.defaultPrepTime = defaultPrepTime;
        this.openingHours = openingHours;
        this.pictureUrls = pictureUrls;
        // A restaurant always has a menu, empty to begin with, so the ten dish
        // rule has something to run against from the start.
        this.menu = RestaurantMenu.forRestaurant(restaurantId);
    }

    /**
     * Creates a new restaurant, works out whether it is open right now, and
     * raises a restaurant created event.
     *
     * @param restaurantId    the restaurant's identifier
     * @param ownerId         the owner it belongs to
     * @param name            the restaurant's name
     * @param address         where it is
     * @param contactEmail    the address customers can reach it on
     * @param cuisineType     the kind of food it serves
     * @param defaultPrepTime how many minutes it usually needs to cook an order
     * @param openingHours    when it is open
     * @param pictureUrls     pictures of the restaurant
     * @return the new restaurant, with its creation event waiting to be published
     */
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

        LocalDateTime now = LocalDateTime.now();
        restaurant.status = restaurant.isOpen(now)
                ? RestaurantStatus.OPEN
                : RestaurantStatus.CLOSED;

        restaurant.eventPublisher.register(new RestaurantCreatedEvent(
                restaurantId.uuid(),
                ownerId.uuid(),
                name,
                cuisineType.toString(),
                defaultPrepTime,
                address.street(),
                String.valueOf(address.number()),
                address.postalCode(),
                address.city(),
                address.coordinates() == null ? null : address.coordinates().latitude(),
                address.coordinates() == null ? null : address.coordinates().longitude(),
                toSlots(openingHours)
        ));

        return restaurant;
    }

    /**
     * Turns the weekly schedule into the shared form other contexts understand.
     *
     * @param openingHours the schedule to convert
     * @return one slot per open day, or an empty list if there is no schedule
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

    /**
     * Sets the status while rebuilding a restaurant from storage.
     *
     * @param status the stored status
     * @return this restaurant, so calls can be chained
     */
    public Restaurant withStatus(RestaurantStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the menu while rebuilding a restaurant from storage.
     *
     * @param menu the stored menu
     * @return this restaurant, so calls can be chained
     * @throws IllegalArgumentException if the menu is missing
     */
    public Restaurant withMenu(RestaurantMenu menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu cannot be null");
        }
        this.menu = menu;
        return this;
    }

    /** Opens the restaurant by hand, ignoring the opening hours (US-09). */
    public void manuallyOpen() {
        if (status == RestaurantStatus.MANUALLY_OPENED) return;
        this.status = RestaurantStatus.MANUALLY_OPENED;
        eventPublisher.register(new RestaurantManuallyOpenedEvent(this.restaurantId.uuid()));
    }

    /** Closes the restaurant by hand, ignoring the opening hours (US-09). */
    public void manuallyClose() {
        if (status == RestaurantStatus.MANUALLY_CLOSED) return;
        this.status = RestaurantStatus.MANUALLY_CLOSED;
        eventPublisher.register(new RestaurantManuallyClosedEvent(this.restaurantId.uuid()));
    }

    /**
     * Drops a manual override so the opening hours decide again (US-09).
     *
     * <p>Does nothing if there is no override in place. Without this an owner who
     * closes early would stay closed for good.
     *
     * @param now the moment to work the new status out from
     */
    public void followSchedule(LocalDateTime now) {
        if (status != RestaurantStatus.MANUALLY_OPENED && status != RestaurantStatus.MANUALLY_CLOSED) {
            return;
        }
        this.status = openingHours.isOpenAt(now.toLocalTime(), now.getDayOfWeek())
                ? RestaurantStatus.OPEN
                : RestaurantStatus.CLOSED;
        eventPublisher.register(new RestaurantScheduleResumedEvent(this.restaurantId.uuid()));
    }

    /**
     * Checks whether the restaurant is taking orders.
     *
     * <p>A manual override wins over the opening hours.
     *
     * @param now the moment to check
     * @return {@code true} if it is open
     */
    public boolean isOpen(LocalDateTime now) {
        if (status == RestaurantStatus.MANUALLY_CLOSED) return false;
        if (status == RestaurantStatus.MANUALLY_OPENED) return true;
        return openingHours.isOpenAt(now.toLocalTime(), now.getDayOfWeek());
    }

    /**
     * Returns the events raised since this restaurant was loaded.
     *
     * @return the events that still need publishing
     */
    public List<DomainEvent> getUncommittedEvents() {
        return eventPublisher.getEvents();
    }

    /** Empties the list of uncommitted events, after they have been published. */
    public void markEventAsCommitted() {
        eventPublisher.clear();
    }

    /** @return the restaurant's identifier */
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    /** @return the owner the restaurant belongs to */
    public OwnerId getOwnerId() {
        return ownerId;
    }

    /** @return the restaurant's name */
    public String getName() {
        return name;
    }

    /** @return where the restaurant is */
    public Address getAddress() {
        return address;
    }

    /** @return the address customers can reach it on */
    public String getContactEmail() {
        return contactEmail;
    }

    /** @return the kind of food it serves */
    public CuisineType getCuisineType() {
        return cuisineType;
    }

    /** @return how many minutes it usually needs to cook an order */
    public int getDefaultPrepTime() {
        return defaultPrepTime;
    }

    /** @return when the restaurant is open */
    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    /** @return pictures of the restaurant */
    public List<String> getPictureUrls() {
        return pictureUrls;
    }

    /** @return whether the restaurant is open, closed, or manually overridden */
    public RestaurantStatus getStatus() {
        return status;
    }

    /** @return the dishes the restaurant currently has live */
    public RestaurantMenu getMenu() {
        return menu;
    }
}
