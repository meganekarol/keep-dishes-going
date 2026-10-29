package be.kdg.keepdishesgoing.pricing.adapter.out.persistence;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.events.PriceRangeCriteriaAdjustedEvent;
import be.kdg.keepdishesgoing.pricing.domain.PriceRangeCriteria;
import be.kdg.keepdishesgoing.pricing.port.out.PriceRangeCriteriaEventStorePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Adapter for loading and saving the price range criteria by event sourcing.
 *
 * <p>No snapshots. The admin adjusts the thresholds a handful of times a year,
 * so the whole log replays in nothing, and a snapshot would only be another
 * thing that could disagree with the events.
 */
@Component
public class PriceRangeCriteriaEventStoreJpaAdapter implements PriceRangeCriteriaEventStorePort {

    private static final Logger log = LoggerFactory.getLogger(PriceRangeCriteriaEventStoreJpaAdapter.class);

    private final PriceRangeCriteriaEventJpaRepository repository;
    private final ObjectMapper objectMapper;

    public PriceRangeCriteriaEventStoreJpaAdapter(PriceRangeCriteriaEventJpaRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // An event store must read what it wrote years ago, so it reads
        // leniently rather than throwing on a property the class has since lost.
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public PriceRangeCriteria load() {
        List<PriceRangeCriteriaEventJpaEntity> events = repository.findAllByOrderBySequenceNumberAsc();

        PriceRangeCriteria criteria = PriceRangeCriteria.blank();
        for (PriceRangeCriteriaEventJpaEntity entity : events) {
            criteria.apply(deserialize(entity));
        }

        log.debug("Rehydrated price range criteria from {} events", events.size());
        return criteria;
    }

    @Override
    public void save(PriceRangeCriteria criteria) {
        List<DomainEvent> events = criteria.getUncommittedEvents();
        if (events.isEmpty()) {
            return;
        }

        int sequence = (int) repository.count();

        for (DomainEvent event : events) {
            try {
                repository.save(new PriceRangeCriteriaEventJpaEntity(
                        event.getClass().getSimpleName(),
                        objectMapper.writeValueAsString(event),
                        event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                        sequence++));
            } catch (JsonProcessingException e) {
                log.error("Error serializing price range criteria event", e);
                throw new IllegalStateException("Failed to save event", e);
            }
        }
    }

    private DomainEvent deserialize(PriceRangeCriteriaEventJpaEntity entity) {
        try {
            return objectMapper.readValue(entity.getEventData(), resolveEventType(entity.getEventType()));
        } catch (JsonProcessingException e) {
            log.error("Error deserializing event {}", entity.getEventType(), e);
            throw new IllegalStateException("Failed to deserialize event", e);
        }
    }

    private Class<? extends DomainEvent> resolveEventType(String eventType) {
        return switch (eventType) {
            case "PriceRangeCriteriaAdjustedEvent" -> PriceRangeCriteriaAdjustedEvent.class;
            default -> throw new IllegalStateException("Unknown pricing event type: " + eventType);
        };
    }
}
