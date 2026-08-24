package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * Where a dish is in its lifecycle.
 *
 * <p>A dish is {@link #DRAFT} until it first goes live. There is no separate
 * "in stock" state: a dish that comes back in stock is {@link #PUBLISHED} again.
 */
public enum DishState {
    DRAFT,
    PUBLISHED,
    UNPUBLISHED,
    OUT_OF_STOCK
}
