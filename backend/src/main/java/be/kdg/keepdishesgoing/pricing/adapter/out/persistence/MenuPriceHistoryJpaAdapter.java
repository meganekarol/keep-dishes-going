package be.kdg.keepdishesgoing.pricing.adapter.out.persistence;

import be.kdg.keepdishesgoing.pricing.domain.valueObjects.MenuPricePoint;
import be.kdg.keepdishesgoing.pricing.port.out.MenuPriceHistoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MenuPriceHistoryJpaAdapter implements MenuPriceHistoryPort {

    private final MenuPricePointJpaRepository repository;

    public MenuPriceHistoryJpaAdapter(MenuPricePointJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MenuPricePoint> historyOf(UUID restaurantId) {
        return repository.findByRestaurantIdOrderByRecordedAtAsc(restaurantId).stream()
                .map(entity -> new MenuPricePoint(entity.getRecordedAt(), entity.getAverageMenuPrice()))
                .toList();
    }
}
