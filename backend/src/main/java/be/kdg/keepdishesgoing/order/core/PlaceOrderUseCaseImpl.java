package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderLine;
import be.kdg.keepdishesgoing.order.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.OrderItemCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.queries.Checkout;
import be.kdg.keepdishesgoing.order.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Turns a basket into an order and sends the customer off to pay (US-22, US-23).
 *
 * <p>The order is saved before the payment is opened, so the provider's callback
 * always has something to attach to.
 */
@Service
@Transactional
public class PlaceOrderUseCaseImpl implements PlaceOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(PlaceOrderUseCaseImpl.class);

    /** Everything is priced in euro; the currency is not a per-order choice. */
    private static final String CURRENCY = "eur";

    private final LoadRestaurantProjectionPort loadRestaurantPort;
    private final LoadDishProjectionPort loadDishPort;
    private final SaveOrderPort updateOrderPort;
    private final OrderEventPublisherPort publishEventPort;
    private final GeocodeDeliveryAddressPort geocodeDeliveryAddressPort;
    private final PaymentProviderPort paymentProviderPort;

    public PlaceOrderUseCaseImpl(
            LoadRestaurantProjectionPort loadRestaurantPort,
            LoadDishProjectionPort loadDishPort,
            SaveOrderPort updateOrderPort,
            OrderEventPublisherPort publishEventPort,
            GeocodeDeliveryAddressPort geocodeDeliveryAddressPort,
            PaymentProviderPort paymentProviderPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
        this.geocodeDeliveryAddressPort = geocodeDeliveryAddressPort;
        this.paymentProviderPort = paymentProviderPort;
    }

    @Override
    public Checkout placeOrder(PlaceOrderCommand command) {
        log.info("Placing order for restaurant: {}", command.restaurantId());

        // Validate restaurant is open
        validateRestaurantOpen(command.restaurantId());

        // Validate all dishes are available and belong to restaurant
        validateDishes(command.items(), command.restaurantId());

        // Resolve the requested items into priced order lines
        List<OrderLine> enrichedOrderLines = toOrderLines(command.items());

        DeliveryAddress deliveryAddress = geocodeDeliveryAddressPort.locate(command.deliveryAddress())
            .map(command.deliveryAddress()::locatedAt)
            .orElse(command.deliveryAddress());

        // Create Order aggregate
        OrderId orderId = OrderId.create();
        Order order = Order.placeOrder(
            orderId,
            command.customerInfo(),
            RestaurantId.of(command.restaurantId()),
            deliveryAddress,
            enrichedOrderLines
        );

        // 6. Save order events to event store
        updateOrderPort.save(order);

        // 7. Publish domain events
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();

        // 8. Open the payment. The order exists at this point but no restaurant
        // has been told about it, and none will be until the provider confirms
        // the money is held (US-23).
        Checkout checkout = openPaymentFor(order, command.restaurantId());

        log.info("Order {} placed, awaiting payment", orderId);
        return checkout;
    }

    private Checkout openPaymentFor(Order order, UUID restaurantId) {
        List<PaymentProviderPort.CheckoutRequest.Line> lines = order.getOrderLines().stream()
                .map(line -> new PaymentProviderPort.CheckoutRequest.Line(
                        line.dishName(), line.unitPrice(), line.quantity()))
                .toList();

        var checkout = paymentProviderPort.openCheckout(new PaymentProviderPort.CheckoutRequest(
                order.getOrderId().uuid(),
                order.getCustomerInfo().contactEmail(),
                loadRestaurantPort.getRestaurantName(restaurantId),
                order.getTotalPrice(),
                CURRENCY,
                lines
        ));

        return new Checkout(order.getOrderId().uuid(), checkout.checkoutUrl());
    }
    
    private void validateRestaurantOpen(UUID restaurantId) {
        boolean isOpen = loadRestaurantPort.isRestaurantOpen(restaurantId);
        if (!isOpen) {
            log.warn("Restaurant {} is closed", restaurantId);
            throw new IllegalStateException("Restaurant is currently closed and cannot accept orders");
        }
    }
    
    private void validateDishes(List<OrderItemCommand> items, UUID restaurantId) {
        List<UUID> dishIds = items.stream()
            .map(OrderItemCommand::dishId)
            .toList();

        // Check all dishes are available
        boolean allAvailable = loadDishPort.areAllDishesAvailable(dishIds);
        if (!allAvailable) {
            log.warn("Some dishes are not available for restaurant {}", restaurantId);
            throw new IllegalStateException("One or more dishes are not available for ordering");
        }
        
        // Verify all dishes belong to the restaurant
        for (UUID dishId : dishIds) {
            if (!loadDishPort.dishBelongsToRestaurant(dishId, restaurantId)) {
                log.error("Dish {} does not belong to restaurant {}", dishId, restaurantId);
                throw new IllegalArgumentException("Dish does not belong to the selected restaurant");
            }
        }
    }
    
    /**
     * Turns the requested dish/quantity pairs into order lines, taking the name
     * and price from the dish projection. Prices are never accepted from the
     * client, and once the resulting event is stored the order total is fixed.
     */
    private List<OrderLine> toOrderLines(List<OrderItemCommand> items) {
        return items.stream()
            .map(item -> new OrderLine(
                item.dishId(),
                loadDishPort.getDishName(item.dishId()),
                loadDishPort.getDishPrice(item.dishId()),
                item.quantity()
            ))
            .toList();
    }
}