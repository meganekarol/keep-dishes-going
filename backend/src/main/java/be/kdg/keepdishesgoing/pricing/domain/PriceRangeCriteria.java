package be.kdg.keepdishesgoing.pricing.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.events.PriceRangeCriteriaAdjustedEvent;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The platform's price range thresholds over time, event sourced (US-32).
 *
 * <p>Event sourcing earns its keep here rather than being decoration. The admin
 * may date a new set of thresholds into the past, so the timeline cannot be a
 * row that gets overwritten. Adjusting appends an event; replaying sorts every
 * event by the date it takes effect, which drops a back-dated version into its
 * place and rewrites every answer that depended on it.
 *
 * <p>One policy governs the whole platform, so there is one aggregate and no id.
 */
public class PriceRangeCriteria {

    private final List<CriteriaVersion> versions = new ArrayList<>();
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private int version;

    private PriceRangeCriteria() {
    }

    /** Blank aggregate used as the starting point for event replay. */
    public static PriceRangeCriteria blank() {
        return new PriceRangeCriteria();
    }

    public void adjust(CriteriaVersion criteria) {
        applyAndRecord(new PriceRangeCriteriaAdjustedEvent(
                criteria.effectiveFrom(),
                criteria.cheapUnder(),
                criteria.regularUnder(),
                criteria.expensiveUnder()));
    }

    /** Used by the persistence adapter to replay historical events during rehydration. */
    public void apply(DomainEvent event) {
        if (event instanceof PriceRangeCriteriaAdjustedEvent adjusted) {
            versions.add(new CriteriaVersion(
                    adjusted.effectiveFrom(),
                    adjusted.cheapUnder(),
                    adjusted.regularUnder(),
                    adjusted.expensiveUnder()));

            // Sorted on the way in, because they do not arrive in the order they
            // take effect. The sort is stable, so two versions dated the same
            // moment stay in the order the admin recorded them and the later one
            // wins in inForceAt.
            versions.sort(Comparator.comparing(CriteriaVersion::effectiveFrom));
        }
        version++;
    }

    private void applyAndRecord(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    /**
     * The thresholds in force at that moment, falling back to the platform's
     * initial ones for any time before the admin set any.
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

    /** Every version the admin has set, in the order they take effect. */
    public List<CriteriaVersion> timeline() {
        return List.copyOf(versions);
    }

    public List<DomainEvent> getUncommittedEvents() {
        return uncommittedEvents;
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    public int getVersion() {
        return version;
    }
}
