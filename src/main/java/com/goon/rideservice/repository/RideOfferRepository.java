package com.goon.rideservice.repository;

import com.goon.rideservice.entity.RideOffer;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
