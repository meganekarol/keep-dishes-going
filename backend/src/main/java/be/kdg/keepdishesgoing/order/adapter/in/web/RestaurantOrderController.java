package be.kdg.keepdishesgoing.order.adapter.in.web;

import be.kdg.keepdishesgoing.order.port.in.*;
import be.kdg.keepdishesgoing.order.port.in.commands.AcceptOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.MarkOrderReadyCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.RejectOrderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for restaurant owner order management operations.
 *
 * <p>Ownership is asserted at class level: every endpoint acts on one
 * restaurant's orders, and the "owner" role alone would let any owner accept
 * or reject another restaurant's orders.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/orders")
@PreAuthorize("@restaurantOwnership.isOwner(#restaurantId, authentication.name)")
public class RestaurantOrderController {
    
    private static final Logger log = LoggerFactory.getLogger(RestaurantOrderController.class);
    
    private final AcceptOrderUseCase acceptOrderUseCase;
    private final RejectOrderUseCase rejectOrderUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    
    public RestaurantOrderController(
            AcceptOrderUseCase acceptOrderUseCase,
            RejectOrderUseCase rejectOrderUseCase,
            MarkOrderReadyUseCase markOrderReadyUseCase) {
        this.acceptOrderUseCase = acceptOrderUseCase;
        this.rejectOrderUseCase = rejectOrderUseCase;
        this.markOrderReadyUseCase = markOrderReadyUseCase;
    }
    
    /**
     * Accept an order
     */
    @PostMapping("/{orderId}/accept")
    public ResponseEntity<OrderActionResponse> acceptOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Restaurant {} accepting order {}", restaurantId, orderId);

        AcceptOrderCommand command = new AcceptOrderCommand(orderId, restaurantId);
        acceptOrderUseCase.acceptOrder(command);

        return ResponseEntity.ok(new OrderActionResponse(
            orderId,
            "Order accepted successfully"
        ));
    }
    
    /**
     * Reject an order
     */
    @PostMapping("/{orderId}/reject")
    public ResponseEntity<OrderActionResponse> rejectOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @RequestBody RejectOrderRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Restaurant {} rejecting order {} with reason: {}", 
            restaurantId, orderId, request.reason);
        
        RejectOrderCommand command = new RejectOrderCommand(
            orderId,
            restaurantId,
            request.reason
        );
        rejectOrderUseCase.rejectOrder(command);

        return ResponseEntity.ok(new OrderActionResponse(
            orderId,
            "Order rejected successfully"
        ));
    }
    
    /**
     * Mark order as ready for pickup
     */
    @PostMapping("/{orderId}/ready")
    public ResponseEntity<OrderActionResponse> markOrderReady(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        
        log.info("Restaurant {} marking order {} as ready", restaurantId, orderId);
        
        MarkOrderReadyCommand command = new MarkOrderReadyCommand(orderId, restaurantId);
        markOrderReadyUseCase.markOrderReady(command);

        return ResponseEntity.ok(new OrderActionResponse(
            orderId,
            "Order marked as ready"
        ));
    }
    
    // DTOs
    public record RejectOrderRequest(String reason) {}
    
    public record OrderActionResponse(UUID orderId, String message) {}
}