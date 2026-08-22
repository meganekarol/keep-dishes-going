package be.kdg.keepdishesgoing.common.exceptions;

import java.util.List;

/**
 * Thrown when an order holds dishes that can no longer be ordered (US-21).
 */
public class DishesUnavailableException extends DomainException {

    private final List<String> dishNames;

    public DishesUnavailableException(List<String> dishNames) {
        super(describe(dishNames));
        this.dishNames = List.copyOf(dishNames);
    }

    public List<String> getDishNames() {
        return dishNames;
    }

    /**
     * Names the dishes rather than counting them: a customer told "one or more"
     * has to empty their basket by trial and error to find out which.
     */
    private static String describe(List<String> dishNames) {
        if (dishNames.size() == 1) {
            return dishNames.get(0) + " is no longer available";
        }
        return String.join(", ", dishNames) + " are no longer available";
    }
}
