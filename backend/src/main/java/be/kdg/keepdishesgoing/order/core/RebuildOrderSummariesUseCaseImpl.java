package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.RebuildOrderSummariesUseCase;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.OrderSummaryData;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderSummaryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Derives the order read model again from the event store.
 *
 * <p>Rebuilt from the aggregates rather than by replaying events into the
 * table: {@link Order} already knows how its own events add up, and a second
 * interpretation of them here would be a second thing to keep correct.
 */
@Service
@Transactional
public class RebuildOrderSummariesUseCaseImpl implements RebuildOrderSummariesUseCase {

    private static final Logger log = LoggerFactory.getLogger(RebuildOrderSummariesUseCaseImpl.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderSummaryPort saveOrderSummaryPort;

    public RebuildOrderSummariesUseCaseImpl(LoadOrderPort loadOrderPort,
                                            SaveOrderSummaryPort saveOrderSummaryPort) {
        this.loadOrderPort = loadOrderPort;
        this.saveOrderSummaryPort = saveOrderSummaryPort;
    }

    @Override
    public int rebuildOrderSummaries() {
        List<UUID> orderIds = loadOrderPort.findAllOrderIds();

        List<OrderSummaryData> summaries = orderIds.stream()
                .map(orderId -> loadOrderPort.loadBy(OrderId.of(orderId)))
                .flatMap(Optional::stream)
                .map(RebuildOrderSummariesUseCaseImpl::toSummary)
                .toList();

        saveOrderSummaryPort.replaceAll(summaries);

        log.info("Rebuilt {} order summaries from {} orders in the event store",
                summaries.size(), orderIds.size());
        return summaries.size();
    }

    private static OrderSummaryData toSummary(Order order) {
        List<OrderSummaryData.ItemLine> lines = order.getOrderLines().stream()
                .map(line -> new OrderSummaryData.ItemLine(line.dishName(), line.quantity()))
                .toList();

        DeliveryAddress address = order.getDeliveryAddress();

        return new OrderSummaryData(
                order.getOrderId().uuid(),
                order.getRestaurantId().uuid(),
                order.getStatus(),
                order.getCustomerInfo().customerName(),
                OrderSummaryData.describeAddress(
                        address.street(), address.number(), address.postalCode(), address.city()),
                OrderSummaryData.describeItems(lines),
                OrderSummaryData.countItems(lines),
                order.getTotalPrice(),
                order.getPlacedAt(),
                order.getAcceptedAt(),
                order.getReadyAt(),
                order.getRejectionReason()
        );
    }
}
