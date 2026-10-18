package com.goon.rideservice.repository;

import com.goon.rideservice.entity.RideOffer;
import com.goon.rideservice.entity.RideOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
 * flushautomatically - hibernarte delays entity modifications to db ,
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
    int declineOffer(UUID offerId,UUID driverId);


    boolean existsByRideIdAndStatus(
            UUID rideId,
            RideOfferStatus status
    );
}
