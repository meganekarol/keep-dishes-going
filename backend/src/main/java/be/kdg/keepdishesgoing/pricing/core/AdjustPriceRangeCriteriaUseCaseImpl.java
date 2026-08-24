package be.kdg.keepdishesgoing.pricing.core;

import be.kdg.keepdishesgoing.pricing.domain.PriceRangeCriteria;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;
import be.kdg.keepdishesgoing.pricing.port.in.AdjustPriceRangeCriteriaUseCase;
import be.kdg.keepdishesgoing.pricing.port.in.commands.AdjustPriceRangeCriteriaCommand;
import be.kdg.keepdishesgoing.pricing.port.out.PriceRangeCriteriaEventStorePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The platform admin setting what the price ranges mean (US-32).
 */
@Service
@Transactional
public class AdjustPriceRangeCriteriaUseCaseImpl implements AdjustPriceRangeCriteriaUseCase {

    private static final Logger log = LoggerFactory.getLogger(AdjustPriceRangeCriteriaUseCaseImpl.class);

    private final PriceRangeCriteriaEventStorePort eventStore;

    public AdjustPriceRangeCriteriaUseCaseImpl(PriceRangeCriteriaEventStorePort eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void adjust(AdjustPriceRangeCriteriaCommand command) {
        PriceRangeCriteria criteria = eventStore.load();

        // Nothing is replaced. A version dated before ones already recorded is
        // still only an append, and the next replay sorts it into place.
        criteria.adjust(new CriteriaVersion(
                command.effectiveFrom(),
                command.cheapUnder(),
                command.regularUnder(),
                command.expensiveUnder()));

        eventStore.save(criteria);
        criteria.clearUncommittedEvents();

        log.info("Price range criteria adjusted, effective from {}", command.effectiveFrom());
    }
}
