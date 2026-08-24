package be.kdg.keepdishesgoing.pricing.port.out;

import be.kdg.keepdishesgoing.pricing.domain.PriceRangeCriteria;

public interface PriceRangeCriteriaEventStorePort {

    /** Rehydrates the timeline by replaying every adjustment ever recorded. */
    PriceRangeCriteria load();

    /** Appends the aggregate's uncommitted events. */
    void save(PriceRangeCriteria criteria);
}
