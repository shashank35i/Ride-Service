// repository/RideRepository.java
package com.goon.rideservice.repository;

import com.goon.rideservice.dto.RideView;
import com.goon.rideservice.entity.Ride;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface RideRepository extends JpaRepository<Ride, UUID> {
    Optional<RideView> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.rideId = :rideId")
    Optional<Ride> findByIdForUpdate(@Param("rideId") UUID rideId);

}
