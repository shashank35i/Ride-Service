// repository/RideRepository.java
package com.goon.rideservice.repository;

import com.goon.rideservice.dto.RideView;
import com.goon.rideservice.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;


public interface RideRepository extends JpaRepository<Ride, UUID> {
    Optional<RideView> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
}