package be.kdg.keepdishesgoing.pricing.port.in;

import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;

import java.util.List;

/**
 * The thresholds the platform has used over time (US-32).
 */
public interface ViewPriceRangeCriteriaQuery {

    /** Every version the admin has set, in the order they take effect. */
    List<CriteriaVersion> timeline();
}
