package be.kdg.keepdishesgoing.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventPublisherHelper {
    private final List<DomainEvent> events = new ArrayList<>();

    public void register(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void clear() {
        events.clear();
    }

}
