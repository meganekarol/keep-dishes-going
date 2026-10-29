package be.kdg.keepdishesgoing.order.adapter.out.projections;

/**
 * Whether the owner has overridden the weekly schedule, and in which direction.
 *
 * <p>{@link #NONE} is the state of a restaurant nobody has toggled yet - the
 * schedule decides. This mirrors the Restaurant context, where
 * {@code RestaurantStatus.OPEN}/{@code CLOSED} are schedule-driven and only the
 * {@code MANUALLY_*} values override them.
 */
public enum ManualOverride {
    NONE,
    FORCED_OPEN,
    FORCED_CLOSED
}
