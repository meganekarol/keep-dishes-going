package be.kdg.keepdishesgoing.pricing.adapter.in;

import be.kdg.keepdishesgoing.pricing.adapter.in.dto.DemoHistoryResponse;
import be.kdg.keepdishesgoing.pricing.adapter.out.persistence.MenuPricePointJpaEntity;
import be.kdg.keepdishesgoing.pricing.adapter.out.persistence.MenuPricePointJpaRepository;
import be.kdg.keepdishesgoing.pricing.adapter.out.projections.MenuDishJpaRepository;
import be.kdg.keepdishesgoing.pricing.port.in.AdjustPriceRangeCriteriaUseCase;
import be.kdg.keepdishesgoing.pricing.port.in.ViewPriceRangeCriteriaQuery;
import be.kdg.keepdishesgoing.pricing.port.in.commands.AdjustPriceRangeCriteriaCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fills in a year of back-dated price history so the evolution graph has
 * something to draw (US-26, US-32).
 */
@Component
public class DemoPriceHistorySeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoPriceHistorySeeder.class);

    /**
     * Thresholds every two months across the past year, rising with inflation.
     * Each row is months ago, then cheap, regular and expensive under.
     */
    private static final int[][] CRITERIA = {
            {12, 8, 20, 40},
            {10, 9, 23, 45},
            {8, 11, 26, 50},
            {6, 13, 29, 55},
            {4, 15, 32, 60},
            {2, 18, 35, 65}
    };

    /**
     * Menu averages, dated between the criteria changes so both timelines put
     * their own moments on the graph. Chosen to cross the thresholds above: the
     * line steps twice because the kitchen repriced, and four times because the
     * bands moved underneath it.
     */
    private static final String[][] AVERAGES = {
            {"13", "8.50"},
            {"9", "24.00"},
            {"5", "30.00"}
    };

    /** Older than this and the restaurant has clearly been seeded already. */
    private static final int SEEDED_AFTER_DAYS = 30;

    private final AdjustPriceRangeCriteriaUseCase adjustPriceRangeCriteriaUseCase;
    private final ViewPriceRangeCriteriaQuery viewPriceRangeCriteriaQuery;
    private final MenuDishJpaRepository dishRepository;
    private final MenuPricePointJpaRepository pricePointRepository;

    public DemoPriceHistorySeeder(AdjustPriceRangeCriteriaUseCase adjustPriceRangeCriteriaUseCase,
                                  ViewPriceRangeCriteriaQuery viewPriceRangeCriteriaQuery,
                                  MenuDishJpaRepository dishRepository,
                                  MenuPricePointJpaRepository pricePointRepository) {
        this.adjustPriceRangeCriteriaUseCase = adjustPriceRangeCriteriaUseCase;
        this.viewPriceRangeCriteriaQuery = viewPriceRangeCriteriaQuery;
        this.dishRepository = dishRepository;
        this.pricePointRepository = pricePointRepository;
    }

    /** Writes the history, and does nothing the second time it is called. */
    @Transactional
    public DemoHistoryResponse seed() {
        LocalDateTime now = LocalDateTime.now();
        int criteriaAdded = seedCriteria(now);
        int restaurantsSeeded = seedPriceHistory(now);

        log.info("Demo price history loaded: {} criteria versions, {} restaurants",
                criteriaAdded, restaurantsSeeded);
        return new DemoHistoryResponse(criteriaAdded, restaurantsSeeded);
    }

    /**
     * Goes through the real use case, so the versions land in the event store as
     * ordinary adjustments rather than as rows only the seeder knows about.
     */
    private int seedCriteria(LocalDateTime now) {
        if (!viewPriceRangeCriteriaQuery.timeline().isEmpty()) {
            log.debug("The admin has already set thresholds, leaving them alone");
            return 0;
        }

        for (int[] version : CRITERIA) {
            adjustPriceRangeCriteriaUseCase.adjust(new AdjustPriceRangeCriteriaCommand(
                    now.minusMonths(version[0]).toLocalDate().atStartOfDay(),
                    BigDecimal.valueOf(version[1]),
                    BigDecimal.valueOf(version[2]),
                    BigDecimal.valueOf(version[3])));
        }
        return CRITERIA.length;
    }

    private int seedPriceHistory(LocalDateTime now) {
        int seeded = 0;

        for (UUID restaurantId : dishRepository.findDistinctRestaurantIds()) {
            if (alreadySeeded(restaurantId, now)) {
                log.debug("Restaurant {} already has back-dated history", restaurantId);
                continue;
            }

            for (String[] average : AVERAGES) {
                pricePointRepository.save(new MenuPricePointJpaEntity(
                        restaurantId,
                        now.minusMonths(Long.parseLong(average[0])),
                        new BigDecimal(average[1])));
            }
            seeded++;
        }
        return seeded;
    }

    private boolean alreadySeeded(UUID restaurantId, LocalDateTime now) {
        List<MenuPricePointJpaEntity> history =
                pricePointRepository.findByRestaurantIdOrderByRecordedAtAsc(restaurantId);

        return !history.isEmpty()
                && history.get(0).getRecordedAt().isBefore(now.minusDays(SEEDED_AFTER_DAYS));
    }
}
