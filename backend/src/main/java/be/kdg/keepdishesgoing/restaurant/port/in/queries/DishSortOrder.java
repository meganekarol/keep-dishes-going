package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import java.util.Comparator;

/**
 * How a menu is ordered for the customer (US-18).
 *
 * <p>Each constant carries its own comparator so callers never re-derive the
 * ordering, and adding a new one cannot be forgotten in a switch somewhere.
 */
public enum DishSortOrder {

    NAME(Comparator.comparing(MenuItem::name, String.CASE_INSENSITIVE_ORDER)),
    PRICE_ASC(Comparator.comparing(MenuItem::price)),
    PRICE_DESC(Comparator.comparing(MenuItem::price).reversed());

    private final Comparator<MenuItem> comparator;

    DishSortOrder(Comparator<MenuItem> comparator) {
        this.comparator = comparator;
    }

    public Comparator<MenuItem> comparator() {
        return comparator;
    }
}
