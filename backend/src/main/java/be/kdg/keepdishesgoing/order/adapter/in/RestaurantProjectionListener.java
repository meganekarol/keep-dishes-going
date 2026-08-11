package be.kdg.keepdishesgoing.order.adapter.in;

import be.kdg.keepdishesgoing.common.events.*;
import be.kdg.keepdishesgoing.order.adapter.out.projections.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens to Restaurant context events and updates projections in Order context.
 * This is how the Order context maintains its read model of restaurants and dishes.
 * 
 * These events are published internally via ApplicationEventPublisher (NOT RabbitMQ).
 */
@Component
public class RestaurantProjectionListener {
    
    private static final Logger log = LoggerFactory.getLogger(RestaurantProjectionListener.class);
    
    private final DishProjectionJpaRepository dishProjectionRepository;
    private final RestaurantProjectionJpaRepository restaurantProjectionRepository;
    
    public RestaurantProjectionListener(DishProjectionJpaRepository dishProjectionRepository,
                                        RestaurantProjectionJpaRepository restaurantProjectionRepository) {
        this.dishProjectionRepository = dishProjectionRepository;
        this.restaurantProjectionRepository = restaurantProjectionRepository;
    }
    
    // ==================== Restaurant Events ====================
    
    @EventListener
    @Transactional
    public void on(RestaurantCreatedEvent event) {
        log.info("Updating projection: Restaurant created - {}", event.restaurantId());
        
        // The schedule travels with the event, so the projection can work out for
        // itself when the restaurant is open instead of waiting for a manual toggle.
        RestaurantProjectionJpaEntity projection = new RestaurantProjectionJpaEntity(
            event.restaurantId(),
            event.name(),
            event.cuisineType(),
            event.openingHours()
        );
        
        restaurantProjectionRepository.save(projection);
        log.debug("Restaurant projection created for: {}", event.restaurantId());
    }
    
    @EventListener
    @Transactional
    public void on(RestaurantManuallyOpenedEvent event) {
        log.info("Updating projection: Restaurant opened - {}", event.restaurantId());
        
        restaurantProjectionRepository.findById(event.restaurantId())
            .ifPresent(restaurant -> {
                restaurant.forceOpen();
                restaurantProjectionRepository.save(restaurant);
            });
    }
    
    @EventListener
    @Transactional
    public void on(RestaurantManuallyClosedEvent event) {
        log.info("Updating projection: Restaurant closed - {}", event.restaurantId());
        
        restaurantProjectionRepository.findById(event.restaurantId())
            .ifPresent(restaurant -> {
                restaurant.forceClosed();
                restaurantProjectionRepository.save(restaurant);
            });
    }
    
    @EventListener
    @Transactional
    public void on(RestaurantScheduleResumedEvent event) {
        log.info("Updating projection: Restaurant back on its schedule - {}", event.restaurantId());

        restaurantProjectionRepository.findById(event.restaurantId())
            .ifPresent(restaurant -> {
                restaurant.followSchedule();
                restaurantProjectionRepository.save(restaurant);
            });
    }

    // ==================== Dish Events ====================
    
    @EventListener
    @Transactional
    public void on(DishPublishedEvent event) {
        log.info("Updating projection: Dish published - {} for restaurant {}", 
                 event.dishId(), event.restaurantId());
        
        // Check if dish already exists in projection (update) or create new
        DishProjectionJpaEntity projection = dishProjectionRepository
            .findById(event.dishId())
            .orElse(new DishProjectionJpaEntity(
                event.dishId(),
                event.restaurantId(),
                event.dishName(),
                event.dishType(),
                event.price(),
                event.description(),
                DishProjectionStatus.AVAILABLE
            ));
        
        // If it was unpublished before, mark as available again
        if (projection.getStatus() == DishProjectionStatus.UNPUBLISHED) {
            projection.markAsAvailable();
        }
        
        dishProjectionRepository.save(projection);
        log.debug("Dish projection updated for: {}", event.dishId());
    }
    
    @EventListener
    @Transactional
    public void on(DishUnpublishedEvent event) {
        log.info("Updating projection: Dish unpublished - {}", event.dishId());
        
        dishProjectionRepository.findById(event.dishId())
            .ifPresentOrElse(dish -> {
                dish.markAsUnpublished();
                dishProjectionRepository.save(dish);
                        log.debug("Dish {} marked as unpublished", event.dishId());
            }, () -> log.warn("Dish projection not found for: {}", event.dishId())
            );
    }
    
    @EventListener
    @Transactional
    public void on(DishMarkedOutOfStockEvent event) {
        log.info("Updating projection: Dish marked out of stock - {}", event.dishId());
        
        dishProjectionRepository.findById(event.dishId())
            .ifPresentOrElse(dish -> {
                dish.markAsOutOfStock();
                dishProjectionRepository.save(dish);
                        log.debug("Dish {} marked as out of stock", event.dishId());
            },
                    () -> log.warn("Dish projection not found for: {}", event.dishId()));
    }
    
    @EventListener
    @Transactional
    public void on(DishMarkedInStockEvent event) {
        log.info("Updating projection: Dish marked in stock - {}", event.dishId());
        
        dishProjectionRepository.findById(event.dishId())
            .ifPresentOrElse(dish -> {
                dish.markAsAvailable();
                dishProjectionRepository.save(dish);
                log.debug("Dish {} marked as available", event.dishId());
            },
                    () -> log.warn("Dish projection not found for: {}", event.dishId())
            );
    }
}