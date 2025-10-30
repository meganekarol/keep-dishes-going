package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.MarkOrderReadyUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.MarkOrderReadyCommand;
import be.kdg.keepdishesgoing.order.port.out.*;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class MarkOrderReadyUseCaseImpl implements MarkOrderReadyUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarkOrderReadyUseCaseImpl.class);

    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort updateOrderPort;
    private final PublishEventPort publishEventPort;

    public MarkOrderReadyUseCaseImpl(
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            PublishEventPort publishEventPort) {
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void markOrderReady(MarkOrderReadyCommand command) {
        log.info("Restaurant {} marking order {} as ready", command.restaurantId(), command.orderId());

        // Load order from event store
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrderPort.loadBy(orderId).orElseThrow();

        // Mark order ready (domain logic)
        order.markReady();

        // Save events
        updateOrderPort.save(order);

        // 5. Create snapshot if needed (every 50 events or when order becomes ready)
        updateOrderPort.saveSnapshot(order);

        // 6. Publish events (includes OrderReadyEvent with @Externalized to RabbitMQ)
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        log.info("Order {} marked as ready by restaurant {}", command.orderId(), command.restaurantId());
    }
}

