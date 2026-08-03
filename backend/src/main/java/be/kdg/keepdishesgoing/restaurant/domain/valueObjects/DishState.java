package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * The lifecycle of a dish. The customer-visible states are exactly three —
 * published, out of stock, unpublished — plus {@link #DRAFT} before a dish has
 * ever gone live. There is deliberately no separate "in stock" state: a dish
 * that is back in stock is simply {@link #PUBLISHED} again.
 */
public enum DishState {
    DRAFT,
    PUBLISHED,
    UNPUBLISHED,
    OUT_OF_STOCK
}
