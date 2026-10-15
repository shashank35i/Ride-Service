// repository/RideRepository.java
package com.goon.rideservice.repository;

import com.goon.rideservice.dto.RideView;
import com.goon.rideservice.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, String> {
    Optional<RideView> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);
}