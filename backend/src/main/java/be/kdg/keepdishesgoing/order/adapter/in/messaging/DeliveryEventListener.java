package be.kdg.keepdishesgoing.order.adapter.in.messaging;

import be.kdg.keepdishesgoing.common.events.OrderDeliveredEvent;
import be.kdg.keepdishesgoing.common.events.OrderLocationEvent;
import be.kdg.keepdishesgoing.common.events.OrderPickedUpEvent;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.in.RecordCourierLocationUseCase;
import be.kdg.keepdishesgoing.order.port.in.UpdateOrderStatusUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.RecordCourierLocationCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.UpdateOrderStatusCommand;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Everything the delivery service tells us about an order in flight (US-29).
 */
@Component
public class DeliveryEventListener {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventListener.class);

    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final RecordCourierLocationUseCase recordCourierLocationUseCase;
    private final ObjectMapper objectMapper;

    public DeliveryEventListener(UpdateOrderStatusUseCase updateOrderStatusUseCase,
                                 RecordCourierLocationUseCase recordCourierLocationUseCase) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
        this.recordCourierLocationUseCase = recordCourierLocationUseCase;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // Their message, their fields. Anything they add that we do not read
        // must not stop us reading the rest.
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @RabbitListener(queues = "keepdishesgoing.delivery-updates")
    public void onDeliveryMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        String body = new String(message.getBody());
        log.info("Delivery message in: {}", routingKey);

        try {
            if (routingKey.endsWith("order.pickedup.v1")) {
                handlePickedUp(body);
            } else if (routingKey.endsWith("order.delivered.v1")) {
                handleDelivered(body);
            } else if (routingKey.endsWith("order.location.v1")) {
                handleLocation(body);
            } else {
                log.warn("Ignoring a delivery message with an unexpected routing key: {}", routingKey);
            }
        } catch (Exception e) {
            log.error("Could not handle delivery message with routing key {}: {}", routingKey, body, e);
        }
    }

    private void handlePickedUp(String body) throws Exception {
        OrderPickedUpEvent event = objectMapper.readValue(body, OrderPickedUpEvent.class);
        log.info("Courier picked up order {}", event.orderId());
        updateOrderStatusUseCase.updateOrderStatus(
                new UpdateOrderStatusCommand(event.orderId(), OrderStatus.IN_DELIVERY));
    }

    private void handleDelivered(String body) throws Exception {
        OrderDeliveredEvent event = objectMapper.readValue(body, OrderDeliveredEvent.class);
        log.info("Order {} was delivered", event.orderId());
        updateOrderStatusUseCase.updateOrderStatus(
                new UpdateOrderStatusCommand(event.orderId(), OrderStatus.DELIVERED));
    }

    private void handleLocation(String body) throws Exception {
        OrderLocationEvent event = objectMapper.readValue(body, OrderLocationEvent.class);
        if (!event.hasPosition()) {
            log.warn("Location message for order {} carried no position: {}", event.orderId(), body);
            return;
        }
        recordCourierLocationUseCase.recordCourierLocation(new RecordCourierLocationCommand(
                event.orderId(), event.latitude(), event.longitude(), event.occurredAt()));
    }
}
