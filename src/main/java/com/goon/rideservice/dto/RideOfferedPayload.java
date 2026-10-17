package com.goon.rideservice.dto;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record RideOfferedPayload(
        UUID offerId,
        UUID rideId,
        UUID driverId,
        double pickupLat,
        double pickupLon,
        double dropLat,
        double dropLon,
        BigDecimal estimatedFare,
        String currency,
        Instant expiresAt,
        int expiresInSeconds
) {}