package be.kdg.keepdishesgoing.restaurant.domain;

import be.kdg.keepdishesgoing.common.EventPublisherHelper;
import be.kdg.keepdishesgoing.common.events.*;
import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One dish on a restaurant's menu.
 *
 * <p>A dish keeps two versions: the one customers can see, and a draft the owner
 * is still working on. The draft is {@code null} when there is nothing pending
 * (US-03).
 */
public class Dish {
    private final DishId dishId;
    private final RestaurantId restaurantId;

    private DishVersion publishedVersion;
    private DishVersion draftVersion;
    private DishState status;
    private LocalDateTime scheduledPublishAt;
    private LocalDateTime lastPublishedAt;

    private final EventPublisherHelper eventPublisher = new EventPublisherHelper();

    private Dish(DishId dishId, RestaurantId restaurantId, DishVersion draftVersion) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;

        this.draftVersion = draftVersion;
        this.status = DishState.DRAFT;
    }

    /**
     * Rebuilds a dish from what was stored.
     *
     * @param dishId             the dish's identifier
     * @param restaurantId       the restaurant it belongs to
     * @param publishedVersion   the version customers see, or {@code null} if it
     *                           has never been published
     * @param draftVersion       the version being worked on, or {@code null}
     * @param status             where the dish is in its lifecycle
     * @param scheduledPublishAt when the pending changes go live, or {@code null}
     * @param lastPublishedAt    when it last went live, or {@code null}
     * @return the rebuilt dish
     */
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

    /**
     * Creates a brand new dish as a draft (US-03).
     *
     * @param dishId         the dish's identifier
     * @param restaurantId   the restaurant it belongs to
     * @param initialVersion the details the owner typed in
     * @return the new dish, not yet visible to customers
     */
    public static Dish createDraft(DishId dishId, RestaurantId restaurantId, DishVersion initialVersion) {
        return new Dish(dishId, restaurantId, initialVersion);
    }

    /**
     * Replaces the draft without touching what customers see (US-03).
     *
     * @param newDraft the new details
     */
    public void editDraft(DishVersion newDraft) {
        this.draftVersion = newDraft;
    }

    /**
     * Puts this dish on the menu (US-04).
     *
     * <p>The draft goes live when there is one. Without a draft the dish keeps
     * the version it had, which is how an unpublished dish goes back on the menu.
     *
     * @throws IllegalStateException if there is nothing to publish
     */
    public void publishDish()  {
        if (!hasDraft() && publishedVersion == null) {
            throw new IllegalStateException("No version to publish");
        }
        if (hasDraft()) {
            this.publishedVersion = draftVersion;
            this.draftVersion = null;
        }
        this.status = DishState.PUBLISHED;
        this.lastPublishedAt = LocalDateTime.now();
        this.scheduledPublishAt = null;

        eventPublisher.register(new DishPublishedEvent(
                this.dishId.uuid(),
                this.restaurantId.uuid(),
                this.publishedVersion.name(),
                this.publishedVersion.type().toString(),
                this.publishedVersion.price(),
                this.publishedVersion.description()
        ));
    }

    /**
     * Takes this dish off the menu (US-05).
     *
     * @throws IllegalStateException if there are unpublished changes waiting
     */
    public void unpublishDish() {
        if (hasDraft()) {
            throw new IllegalStateException("Cannot unpublish a draft dish");
        }
        this.status = DishState.UNPUBLISHED;
        eventPublisher.register(new DishUnpublishedEvent(
                this.dishId.uuid(),
                this.restaurantId.uuid()
        ));
    }

    /**
     * Marks this dish sold out. It stays on the menu but cannot be ordered (US-08).
     *
     * @throws IllegalArgumentException if the dish is not currently published
     */
    public void markDishOutOfStock() {
        if (!isPublished()) {
            throw new IllegalArgumentException(
                    "Only published dishes can be marked out of stock. Current status: " + status
            );
        }
        this.status = DishState.OUT_OF_STOCK;
        eventPublisher.register(new DishMarkedOutOfStockEvent(
                this.dishId.uuid(),
                this.restaurantId.uuid(),
                this.publishedVersion.name()
        ));
    }

    /**
     * Puts a sold-out dish back on sale (US-08).
     *
     * <p>Goes back to {@link DishState#PUBLISHED}, because being in stock is the
     * same thing as being published.
     *
     * @throws IllegalArgumentException if the dish is not currently sold out
     */
    public void markDishInStock() {
        if (status != DishState.OUT_OF_STOCK) {
            throw new IllegalArgumentException(
                    "Only out-of-stock dishes can be marked in stock. Current status: " + status
            );
        }
        this.status = DishState.PUBLISHED;
        eventPublisher.register(new DishMarkedInStockEvent(
                this.dishId.uuid(),
                this.restaurantId.uuid()
        ));
    }

    /**
     * Books the pending changes to go live later (US-07).
     *
     * @param dateTime when the changes should go live
     * @throws IllegalStateException    if there are no pending changes
     * @throws IllegalArgumentException if the time is in the past
     */
    public void scheduleChanges(LocalDateTime dateTime) {
        if (!hasPendingChanges()) {
            throw new IllegalStateException("No pending changes to schedule.");
        }
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule publishing in the past.");
        }

        this.scheduledPublishAt = dateTime;
    }

    /**
     * Returns the events raised since this dish was loaded.
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

    /**
     * Checks whether the owner has changes waiting to go live.
     *
     * @return {@code true} if there is a draft that differs from the live version
     */
    public boolean hasPendingChanges() {
        return draftVersion != null && !draftVersion.equals(publishedVersion);
    }

    /**
     * Checks whether a draft exists.
     *
     * @return {@code true} if the owner has a draft
     */
    public boolean hasDraft() {
        return draftVersion != null;
    }

    /**
     * Checks whether the dish can be ordered right now.
     *
     * @return {@code true} if it is published and in stock
     */
    public boolean isPublished() {
        return status == DishState.PUBLISHED;
    }

    /**
     * Checks whether customers see this dish on the menu.
     *
     * <p>Sold-out dishes stay on it, so a customer can tell the difference
     * between a dish that is back tomorrow and one that is gone.
     *
     * @return {@code true} if the dish is published or sold out
     */
    public boolean isOnMenu() {
        return status == DishState.PUBLISHED || status == DishState.OUT_OF_STOCK;
    }

    /** @return the dish's identifier */
    public DishId getDishId() {
        return dishId;
    }

    /** @return the restaurant the dish belongs to */
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    /** @return the version customers see, or {@code null} if it has never been published */
    public DishVersion getPublishedVersion() {
        return publishedVersion;
    }

    /** @return the version being worked on, or {@code null} if there is none */
    public DishVersion getDraftVersion() {
        return draftVersion;
    }

    /** @return where the dish is in its lifecycle */
    public DishState getStatus() {
        return status;
    }

    /** @return when the pending changes go live, or {@code null} if none are booked */
    public LocalDateTime getScheduledPublishAt() {
        return scheduledPublishAt;
    }

    /** @return when the dish last went live, or {@code null} if it never has */
    public LocalDateTime getLastPublishedAt() {
        return lastPublishedAt;
    }
}
