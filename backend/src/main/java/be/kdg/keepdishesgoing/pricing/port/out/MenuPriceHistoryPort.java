package be.kdg.keepdishesgoing.pricing.port.out;

import be.kdg.keepdishesgoing.pricing.domain.valueObjects.MenuPricePoint;

import java.util.List;
import java.util.UUID;

public interface MenuPriceHistoryPort {

    /**
     * Every average this restaurant's menu has had, oldest first.
     *
     * <p>Empty for a restaurant that has never published a dish since the
     * platform started keeping this history.
     */
    List<MenuPricePoint> historyOf(UUID restaurantId);
}
