package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.RestaurantMenu;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ViewOwnerMenuUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.OwnerDishView;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.OwnerMenu;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Serves the owner's own menu.
 *
 * <p>Loads the restaurant even though only its dishes are asked for: the cap on
 * published dishes is enforced against the menu inside the aggregate, so that is
 * where the live count has to be read from for the screen to agree with what the
 * next publish will allow.
 */
@Service
@Transactional(readOnly = true)
public class ViewOwnerMenuUseCaseImpl implements ViewOwnerMenuUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final LoadDishPort loadDishPort;

    public ViewOwnerMenuUseCaseImpl(LoadRestaurantPort loadRestaurantPort, LoadDishPort loadDishPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
    }

    @Override
    public OwnerMenu viewOwnerMenu(UUID restaurantId) {
        RestaurantId id = RestaurantId.of(restaurantId);
        Restaurant restaurant = loadRestaurantPort.loadById(id)
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));

        List<OwnerDishView> dishes = loadDishPort.loadByRestaurant(id).stream()
                .map(OwnerDishView::from)
                .sorted(Comparator.comparing(ViewOwnerMenuUseCaseImpl::displayName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        long pending = dishes.stream().filter(OwnerDishView::pendingChanges).count();

        return new OwnerMenu(
                restaurantId,
                dishes,
                (int) pending,
                restaurant.getMenu().getPublishedDishes().size(),
                RestaurantMenu.maxPublishedDishes()
        );
    }

    /**
     * The name the owner recognises the dish by: the live one where there is one,
     * and the draft for a dish that has never gone live. Sorting on it keeps the
     * list in one place while a dish is being edited, which sorting on the draft
     * alone would not.
     */
    private static String displayName(OwnerDishView dish) {
        return dish.published() != null ? dish.published().name() : dish.draft().name();
    }
}
