package com.goon.rideservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Holds the driver's single active ride assignment. */
@Entity
@Table(
        name = "driver_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_driver_assignments_ride_id",
                columnNames = "ride_id"
        )
)
public class DriverAssignment {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "driver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID driverId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "ride_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID rideId;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    protected DriverAssignment() {}

    public DriverAssignment(UUID driverId, UUID rideId, Instant assignedAt) {
        this.driverId = driverId;
        this.rideId = rideId;
        this.assignedAt = assignedAt;
    }
}
