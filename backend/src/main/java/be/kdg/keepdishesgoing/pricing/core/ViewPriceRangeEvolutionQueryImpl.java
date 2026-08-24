package be.kdg.keepdishesgoing.pricing.core;

import be.kdg.keepdishesgoing.pricing.domain.PriceRangeCriteria;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.MenuPricePoint;
import be.kdg.keepdishesgoing.pricing.port.in.ViewPriceRangeCriteriaQuery;
import be.kdg.keepdishesgoing.pricing.port.in.ViewPriceRangeEvolutionQuery;
import be.kdg.keepdishesgoing.pricing.port.in.queries.PriceRangeEvolution;
import be.kdg.keepdishesgoing.pricing.port.in.queries.PriceRangePoint;
import be.kdg.keepdishesgoing.pricing.port.out.MenuPriceHistoryPort;
import be.kdg.keepdishesgoing.pricing.port.out.PriceRangeCriteriaEventStorePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds the evolution graph by joining two timelines (US-26).
 *
 * <p>Computed on every read and never cached. The admin can date new thresholds
 * into the past, which rewrites history for every restaurant at once; a stored
 * projection would have to be found and rebuilt, and this cannot go stale.
 */
@Service
@Transactional(readOnly = true)
public class ViewPriceRangeEvolutionQueryImpl
        implements ViewPriceRangeEvolutionQuery, ViewPriceRangeCriteriaQuery {

    private final MenuPriceHistoryPort menuPriceHistoryPort;
    private final PriceRangeCriteriaEventStorePort criteriaEventStore;

    public ViewPriceRangeEvolutionQueryImpl(MenuPriceHistoryPort menuPriceHistoryPort,
                                            PriceRangeCriteriaEventStorePort criteriaEventStore) {
        this.menuPriceHistoryPort = menuPriceHistoryPort;
        this.criteriaEventStore = criteriaEventStore;
    }

    @Override
    public List<CriteriaVersion> timeline() {
        return criteriaEventStore.load().timeline();
    }

    @Override
    public PriceRangeEvolution evolutionOf(UUID restaurantId) {
        List<MenuPricePoint> history = menuPriceHistoryPort.historyOf(restaurantId);
        if (history.isEmpty()) {
            return new PriceRangeEvolution(restaurantId, List.of());
        }

        PriceRangeCriteria criteria = criteriaEventStore.load();

        List<PriceRangePoint> points = momentsThatMatter(history, criteria).stream()
                .map(moment -> {
                    BigDecimal average = averageAt(history, moment);
                    return new PriceRangePoint(moment, average, criteria.inForceAt(moment).classify(average));
                })
                .toList();

        return new PriceRangeEvolution(restaurantId, points);
    }

    /**
     * Every moment the graph can change shape.
     *
     * <p>Both timelines count. The line steps when the menu gets dearer, and it
     * steps again when the platform changes what "dear" means, which is the
     * whole point of showing the evolution against today's standards.
     */
    private List<LocalDateTime> momentsThatMatter(List<MenuPricePoint> history,
                                                  PriceRangeCriteria criteria) {
        LocalDateTime firstRecorded = history.get(0).at();
        LocalDateTime now = LocalDateTime.now();

        List<LocalDateTime> moments = new ArrayList<>();
        history.forEach(point -> moments.add(point.at()));

        criteria.timeline().stream()
                .map(CriteriaVersion::effectiveFrom)
                // Before the restaurant had a menu there is nothing to classify,
                // and thresholds dated ahead of today are not history yet.
                .filter(from -> from.isAfter(firstRecorded) && !from.isAfter(now))
                .forEach(moments::add);

        return moments.stream().distinct().sorted().toList();
    }

    /** The last average recorded at or before that moment. */
    private BigDecimal averageAt(List<MenuPricePoint> history, LocalDateTime moment) {
        BigDecimal average = history.get(0).averageMenuPrice();
        for (MenuPricePoint point : history) {
            if (point.at().isAfter(moment)) {
                break;
            }
            average = point.averageMenuPrice();
        }
        return average;
    }
}
