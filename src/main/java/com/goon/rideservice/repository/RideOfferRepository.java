package com.goon.rideservice.repository;

import com.goon.rideservice.entity.RideOffer;
import com.goon.rideservice.entity.RideOfferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RideOfferRepository extends JpaRepository<RideOffer, UUID> {
    @Query("""
        SELECT o.driverId
        FROM RideOffer o
        WHERE o.rideId = :rideId
        """)
    Set<UUID> findOfferedDriverIds(@Param("rideId") UUID rideId);


/**
 * clearautomatically - query way bypasses springs entity manager lifecycle ,
 * which can leave first level cache of application,
 * so new updates wont be shown rightafter running query method if clearautomatically isnt true.
 *
 *
 * flushautomatically - hibernarte sends data to db only after method ends, entity modifications to db ,
 * until updates in memory are executed, which can make new modifications work on stale data
 *
  */

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query(value = """
         UPDATE ride_offers
         SET status= 'DECLINED'
         WHERE offer_id = :offerId
         AND driver_id = :driverId
         AND status = 'OFFERED'         
            """, nativeQuery = true)
    int declineOffer(@Param("offerId") UUID offerId,@Param("driverId") UUID driverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM RideOffer o WHERE o.offerId = :offerId")
    Optional<RideOffer> findByIdForUpdate(@Param("offerId") UUID offerId);

    boolean existsByRideIdAndStatus(
            UUID rideId,
            RideOfferStatus status
    );



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
    SET o.status = RideOfferStatus.EXPIRED
    WHERE o.offerId = :offerId
    AND o.status = RideOfferStatus.OFFERED
    AND o.expiresAt <= :now
    """)
    int expireOffer(
            @Param("offerId") UUID offerId,
            @Param("now") Instant now
    );
}
