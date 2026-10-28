package com.goon.rideservice.entity;

import com.goon.rideservice.service.VehicleType;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "rides",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_idempotency",
                        columnNames = {"customer_id", "idempotency_key"}
                ),
                @UniqueConstraint(
                        name = "uk_quote_id",
                        columnNames = {"quote_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_customer_status",
                        columnList = "customer_id,status"
                ),
                @Index(
                        name = "idx_fare_sweeper",
                        columnList = "payment_status,next_fare_attempt_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ride {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "ride_id", columnDefinition = "BINARY(16)")
    private UUID rideId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "customer_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "driver_id", columnDefinition = "BINARY(16)")
    private UUID driverId;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "quote_id", length = 64)
    private String quoteId;

    @Column(name = "pickup_lat", nullable = false)
    private double pickupLat;

    @Column(name = "pickup_lon", nullable = false)
    private double pickupLon;

    @Column(name = "drop_lat", nullable = false)
    private double dropLat;

    @Column(name = "drop_lon", nullable = false)
    private double dropLon;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 32)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 32)
    private RideStatus status = RideStatus.REQUESTED;

    @Column(name = "quoted_fare", precision = 12, scale = 2)
    private BigDecimal quotedFare;

    @Column(name = "final_fare", precision = 12, scale = 2)
    private BigDecimal finalFare;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.NOT_STARTED;

    @Builder.Default
    @Column(name = "payment_revision", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long paymentRevision = 0L;

    @Column(name = "fare_source", length = 16)
    private String fareSource;

    @Builder.Default
    @Column(name = "fare_attempts", nullable = false)
    private Integer fareAttempts = 0;

    @Column(name = "next_fare_attempt_at")
    private Instant nextFareAttemptAt;

    @Column(name = "start_otp", length = 8)
    private String startOtp;

    @Builder.Default
    @Column(name = "otp_attempts", nullable = false)
    private Integer otpAttempts = 0;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "vehicle_model", length = 64)
    private String vehicleModel;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Builder.Default
    @Column(name = "distance_m", nullable = false)
    private Double distanceM = 0.0;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lon")
    private Double lastLon;

    @Column(name = "last_ts")
    private Long lastTs;

    @Builder.Default
    @Column(name = "distance_final", nullable = false)
    private Boolean distanceFinal = false;

    @Builder.Default
    @Column(name = "waiting_time_s", nullable = false)
    private Integer waitingTimeS = 0;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "arrived_at")
    private Instant arrivedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by", length = 16)
    private String cancelledBy;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @PrePersist
    void onCreate() {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
        if (startOtp == null) {
            startOtp = String.format("%04d", RANDOM.nextInt(10000));   ///take until 9999 , dont invlove 10000  . andonly 4 digits   , fill rest with 0s.
        }
    }
}
