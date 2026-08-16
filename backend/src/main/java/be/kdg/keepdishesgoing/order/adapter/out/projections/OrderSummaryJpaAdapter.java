package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.LoadOrderSummaryPort;
import be.kdg.keepdishesgoing.order.port.out.OrderSummaryData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Reads the order read model and maps the rows onto the outbound port's record.
 */
@Component
public class OrderSummaryJpaAdapter implements LoadOrderSummaryPort {

    private final OrderSummaryJpaRepository summaryRepository;

    public OrderSummaryJpaAdapter(OrderSummaryJpaRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public List<OrderSummaryData> loadByRestaurant(UUID restaurantId) {
        return summaryRepository.findByRestaurantIdOrderByPlacedAtDesc(restaurantId)
                .stream()
                .map(OrderSummaryJpaAdapter::toData)
                .toList();
    }

    private static OrderSummaryData toData(OrderSummaryJpaEntity entity) {
        return new OrderSummaryData(
                entity.getOrderId(),
                entity.getStatus(),
                entity.getCustomerName(),
                entity.getDeliveryAddress(),
                entity.getItemSummary(),
                entity.getItemCount(),
                entity.getTotalPrice(),
                entity.getPlacedAt(),
                entity.getAcceptedAt(),
                entity.getReadyAt(),
                entity.getRejectionReason()
        );
    }
}
