package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.in.RecordCourierLocationUseCase;
import be.kdg.keepdishesgoing.order.port.in.UpdateOrderStatusUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.RecordCourierLocationCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.UpdateOrderStatusCommand;
import be.kdg.keepdishesgoing.order.port.out.CourierLocationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records where a courier is (US-29).
 *
 * <p>Positions themselves never touch the order aggregate: rehydrating an
 * event-sourced order for every GPS ping would be work spent to change nothing,
 * and one order event per ping would bury the events that matter.
 *
 * <p>The first position is different. The delivery service is supposed to
 * announce a pickup and does not, so without this an order stays at "waiting
 * for a courier" for the whole journey and only jumps to delivered at the end.
 * A courier reporting a position has the food and is moving, which is exactly
 * what "on its way" means to a customer (US-25).
 */
@Service
@Transactional
public class RecordCourierLocationUseCaseImpl implements RecordCourierLocationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordCourierLocationUseCaseImpl.class);

    private final CourierLocationPort courierLocationPort;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public RecordCourierLocationUseCaseImpl(CourierLocationPort courierLocationPort,
                                            UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.courierLocationPort = courierLocationPort;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    @Override
    public void recordCourierLocation(RecordCourierLocationCommand command) {
        boolean firstReport = courierLocationPort.record(
                command.orderId(), command.lat(), command.lng(), command.reportedAt());

        if (!firstReport) {
            return;
        }

        // Only on the first position, so the aggregate is loaded once per order
        // rather than once per ping. Already-moving orders are left alone by
        // the use case below.
        log.info("First position for order {}, treating it as picked up", command.orderId());
        updateOrderStatusUseCase.updateOrderStatus(
                new UpdateOrderStatusCommand(command.orderId(), OrderStatus.IN_DELIVERY));
    }
}
