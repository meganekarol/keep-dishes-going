package be.kdg.keepdishesgoing.order.adapter.in.scheduling;

import be.kdg.keepdishesgoing.order.adapter.out.persistence.OrderEventJpaEntity;
import be.kdg.keepdishesgoing.order.adapter.out.persistence.OrderEventJpaRepository;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.OrderEventPublisherPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Scheduler that automatically rejects orders that haven't been accepted within 5 minutes
 */
@Component
public class OrderAutoRejectionScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(OrderAutoRejectionScheduler.class);
    private static final int ACCEPTANCE_TIMEOUT_MINUTES = 5;
    
    private final OrderEventJpaRepository eventRepository;
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort updateOrderPort;
    private final OrderEventPublisherPort publishEventPort;
    
    public OrderAutoRejectionScheduler(
            OrderEventJpaRepository eventRepository,
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            OrderEventPublisherPort publishEventPort) {
        this.eventRepository = eventRepository;
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }
    
    /**
     * Check for pending orders every minute
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    @Transactional
    public void checkPendingOrders() {
        log.debug("Checking for orders pending acceptance...");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(ACCEPTANCE_TIMEOUT_MINUTES);

        // Find orders placed more than 5 minutes ago; loading each order tells us
        // whether it is still pending acceptance (rejected/accepted ones are skipped).
        List<UUID> staleOrderIds = eventRepository
            .findByEventTypeAndOccurredOnBefore("OrderPlacedEvent", cutoffTime)
            .stream()
            .map(OrderEventJpaEntity::getOrderId)
            .distinct()
            .toList();

        for (UUID orderId : staleOrderIds) {
            try {
                autoRejectOrder(orderId);
            } catch (Exception e) {
                log.error("Error auto-rejecting order {}", orderId, e);
            }
        }
    }
    
    private void autoRejectOrder(UUID orderId) {
        log.info("Auto-rejecting order {} due to timeout", orderId);
        
        // Load order
        Order order = loadOrderPort.loadBy(OrderId.of(orderId)).orElseThrow();
        
        // Only reject if still pending
        if (order.isPendingAcceptance()) {
            order.autoReject();
            
            // Save and publish
            updateOrderPort.save(order);
            publishEventPort.publishAll(order.getUncommittedEvents());
            order.clearUncommittedEvents();
            
            log.info("Order {} automatically rejected", orderId);
        }
    }
}