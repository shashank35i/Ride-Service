package com.goon.rideservice.repository;

import com.goon.rideservice.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {

    @Query(value = """
            SELECT * FROM outbox
            WHERE published = false
            ORDER BY created_at, event_id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Outbox> fetchBatch(@Param("limit") int limit);

    @Query("""
        SELECT o.offerId
        FROM RideOffer o
        WHERE o.status = RideOfferStatus.OFFERED
        AND o.expiresAt <= :now
        """)
    List<UUID> findExpiredOfferIds(
            @Param("now") Instant now,
            Pageable pageable
    );


    @Modifying
    @Query("""
    UPDATE RideOffer o
    SET o.status = com.goon.rideservice.entity.RideOfferStatus.EXPIRED
    WHERE o.offerId = :offerId
    AND o.status = com.goon.rideservice.entity.RideOfferStatus.OFFERED
    AND o.expiresAt <= :now
    """)
    int expireOffer(
            @Param("offerId") UUID offerId,
            @Param("now") Instant now
    );
}
