package com.starline.resi.repository;

import com.starline.resi.dto.projection.ResiProjection;
import com.starline.resi.model.Resi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResiRepository extends JpaRepository<Resi, String> {

    int countByUserId(Long userId);

    int countByTrackingNumberAndUserId(String trackingNumber, Long userId);

    @Query("""
        SELECT r.trackingNumber as trackingNumber, r.userId as userId,
        r.lastCheckpoint as lastCheckpoint, r.additionalValue1 as additionalValue1,
        r.courier.code as courierCode, r.lastCheckpointUpdate as lastCheckpointUpdate,
        r.courier.name as courierName, r.courier.id as courierId,
        r.originalCheckpointTime as originalCheckpointTime
        FROM Resi r
        WHERE r.userId = :userId
    """)
    List<ResiProjection> getResiByUserId(Long userId);

    void deleteByTrackingNumberAndUserId(String trackingNumber, Long userId);


    int deleteAllByCreatedDateBefore(LocalDateTime createdDate);

    int deleteAllBySubscriptionExpiryDateLessThanEqual(LocalDate subscriptionExpiryDate);

    Optional<Resi> findByTrackingNumberAndUserId(String trackingNumber, Long userId);
}
