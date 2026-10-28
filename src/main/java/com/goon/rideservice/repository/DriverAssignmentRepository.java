package com.goon.rideservice.repository;

import com.goon.rideservice.entity.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, UUID> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT IGNORE INTO driver_assignments (driver_id, ride_id, assigned_at)
            VALUES (:driverId, :rideId, :assignedAt)
            """, nativeQuery = true)
    int claim(@Param("driverId") UUID driverId,
              @Param("rideId") UUID rideId,
              @Param("assignedAt") Instant assignedAt);

    @Modifying
    @Query(value = """
            DELETE FROM driver_assignments
            WHERE driver_id = :driverId AND ride_id = :rideId
            """, nativeQuery = true)
    int release(@Param("driverId") UUID driverId, @Param("rideId") UUID rideId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO driver_assignments (driver_id, ride_id, assigned_at)
            SELECT driver_id, ride_id, COALESCE(assigned_at, requested_at)
            FROM rides
            WHERE driver_id IS NOT NULL
              AND status IN ('ASSIGNED', 'DRIVER_ARRIVED', 'STARTED')
            ORDER BY COALESCE(assigned_at, requested_at), ride_id
            """, nativeQuery = true)
    int seedCurrentAssignments();
}
