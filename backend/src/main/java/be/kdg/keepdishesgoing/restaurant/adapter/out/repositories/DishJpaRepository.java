package be.kdg.keepdishesgoing.restaurant.adapter.out.repositories;

import be.kdg.keepdishesgoing.restaurant.adapter.out.DishJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DishJpaRepository extends JpaRepository<DishJpaEntity, UUID> {
    @Query("""
    SELECT d FROM DishJpaEntity d
    WHERE d.restaurantId = :restaurantId
      AND d.draftVersion IS NOT NULL
""")
    List<DishJpaEntity> findPendingChangesByRestaurant(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT d FROM DishJpaEntity d
        WHERE d.scheduledPublishAt IS NOT NULL
          AND d.scheduledPublishAt <= :now
    """)
    List<DishJpaEntity> findReadyForScheduledPublish(@Param("now") LocalDateTime now);

    @Query("""
        SELECT d FROM DishJpaEntity d
        WHERE d.restaurantId = :restaurantId
    """)
    List<DishJpaEntity> findAllByRestaurant(@Param("restaurantId") UUID restaurantId);
}
