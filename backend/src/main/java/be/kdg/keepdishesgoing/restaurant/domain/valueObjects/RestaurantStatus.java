package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

public enum RestaurantStatus {
    OPEN,           // open according to schedule or manual override
    CLOSED,         // closed according to schedule
    MANUALLY_OPENED,
    MANUALLY_CLOSED
}
