package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderSummaryPort;
import be.kdg.keepdishesgoing.order.port.out.OrderSummaryData;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderSummaryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Reads the order read model and maps the rows onto the outbound port's record.
 */
@Component
public class OrderSummaryJpaAdapter implements LoadOrderSummaryPort, SaveOrderSummaryPort {

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

    @Override
    public List<UUID> findPendingAcceptanceBefore(LocalDateTime cutoff) {
        return summaryRepository.findIdsByStatusPlacedBefore(OrderStatus.PENDING_ACCEPTANCE, cutoff);
    }

    /** Placed but not yet cooked: the two states that keep a kitchen busy. */
    private static final List<OrderStatus> STILL_IN_THE_KITCHEN =
            List.of(OrderStatus.PENDING_ACCEPTANCE, OrderStatus.ACCEPTED);

    @Override
    public int countNotReadyForRestaurant(UUID restaurantId) {
        return (int) summaryRepository.countByRestaurantIdAndStatusIn(restaurantId, STILL_IN_THE_KITCHEN);
    }

    @Override
    public void replaceAll(List<OrderSummaryData> summaries) {
        summaryRepository.deleteAllInBatch();
        summaryRepository.saveAll(summaries.stream().map(OrderSummaryJpaAdapter::toEntity).toList());
    }

    private static OrderSummaryJpaEntity toEntity(OrderSummaryData data) {
        OrderSummaryJpaEntity entity = new OrderSummaryJpaEntity(
                data.orderId(),
                data.restaurantId(),
                data.customerName(),
                data.deliveryAddress(),
                data.itemSummary(),
                data.itemCount(),
                data.totalPrice(),
                data.placedAt()
        );
        entity.restoreProgress(data.status(), data.acceptedAt(), data.readyAt(), data.rejectionReason());
        return entity;
    }

    private static OrderSummaryData toData(OrderSummaryJpaEntity entity) {
        return new OrderSummaryData(
                entity.getOrderId(),
                entity.getRestaurantId(),
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
