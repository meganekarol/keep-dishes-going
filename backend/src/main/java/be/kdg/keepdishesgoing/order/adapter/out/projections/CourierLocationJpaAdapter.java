package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.CourierLocationPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
public class CourierLocationJpaAdapter implements CourierLocationPort {

    private final CourierLocationJpaRepository repository;

    public CourierLocationJpaAdapter(CourierLocationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean record(UUID orderId, double lat, double lng, LocalDateTime reportedAt) {
        CourierLocationJpaEntity existing = repository.findById(orderId).orElse(null);
        boolean first = existing == null;

        CourierLocationJpaEntity location = first
                ? new CourierLocationJpaEntity(orderId, lat, lng, reportedAt)
                : existing;
        location.reportedAt(lat, lng, reportedAt);
        repository.save(location);

        return first;
    }

    @Override
    public Optional<CourierLocation> findByOrder(UUID orderId) {
        return repository.findById(orderId)
                .map(entity -> new CourierLocation(
                        entity.getLat(), entity.getLng(), entity.getReportedAt()));
    }
}
