package com.goon.rideservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "ride_offers",
        indexes = {
                @Index(
                        name = "idx_ride_offers_status_expires",
                        columnList = "status, expires_at"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "offer_id", columnDefinition = "BINARY(16)")
    private UUID offerId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "ride_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID rideId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "driver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID driverId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RideOfferStatus status = RideOfferStatus.OFFERED;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}