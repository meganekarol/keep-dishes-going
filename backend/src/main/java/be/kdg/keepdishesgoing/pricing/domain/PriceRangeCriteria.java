package be.kdg.keepdishesgoing.pricing.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.events.PriceRangeCriteriaAdjustedEvent;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The platform's price range thresholds over time (US-32).
 *
 * <p>Kept as a list of versions, each with the date it starts to count. There is
 * one set of criteria for the whole platform, so this aggregate has no id.
 */
public class PriceRangeCriteria {

    private final List<CriteriaVersion> versions = new ArrayList<>();
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private int version;

    private PriceRangeCriteria() {
    }

    /**
     * Creates an empty aggregate to replay events onto.
     *
     * @return a new aggregate with no versions yet
     */
    public static PriceRangeCriteria blank() {
        return new PriceRangeCriteria();
    }

    /**
     * Records a new set of thresholds.
     *
     * @param criteria the new thresholds and the date they start to count
     */
    public void adjust(CriteriaVersion criteria) {
        applyAndRecord(new PriceRangeCriteriaAdjustedEvent(
                criteria.effectiveFrom(),
                criteria.cheapUnder(),
                criteria.regularUnder(),
                criteria.expensiveUnder()));
    }

    /**
     * Applies one event to the aggregate. Used when replaying stored events.
     *
     * @param event the event to apply, ignored if it is not a criteria adjustment
     */
    public void apply(DomainEvent event) {
        if (event instanceof PriceRangeCriteriaAdjustedEvent adjusted) {
            versions.add(new CriteriaVersion(
                    adjusted.effectiveFrom(),
                    adjusted.cheapUnder(),
                    adjusted.regularUnder(),
                    adjusted.expensiveUnder()));

            // Events do not arrive in the order they take effect, so sort on the
            // way in. The sort is stable, so two versions dated the same moment
            // keep the order the admin recorded them in.
            versions.sort(Comparator.comparing(CriteriaVersion::effectiveFrom));
        }
        version++;
    }

    private void applyAndRecord(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * Finds the thresholds that were in force at a given moment.
     *
     * @param moment the point in time to look up
     * @return the thresholds in force then, or {@code CriteriaVersion.INITIAL} if
     *         the admin had not set any yet
     */
    public CriteriaVersion inForceAt(LocalDateTime moment) {
        CriteriaVersion inForce = CriteriaVersion.INITIAL;
        for (CriteriaVersion candidate : versions) {
            if (candidate.effectiveFrom().isAfter(moment)) {
                break;
            }
            inForce = candidate;
        }
        return inForce;
    }

    /**
     * Lists every set of thresholds the admin has recorded.
     *
     * @return the versions, in the order they take effect
     */
    public List<CriteriaVersion> timeline() {
        return List.copyOf(versions);
    }

    /**
     * Returns the events raised since this aggregate was loaded.
     *
     * @return the events that still need saving
     */
    public List<DomainEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }

    /** Empties the list of uncommitted events, after they have been saved. */
    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    /**
     * Returns how many events have been applied to this aggregate.
     *
     * @return the version number
     */
    public int getVersion() {
        return version;
    }
}
