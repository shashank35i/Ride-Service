package com.goon.rideservice.dto.response;

import com.goon.rideservice.dto.RideView;
import com.goon.rideservice.entity.Ride;
import com.goon.rideservice.entity.RideStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record RideResponse(
        UUID rideId,
        RideStatus status,
        BigDecimal estimatedFare,
        BigDecimal finalFare,
        String currency
) {
    public static RideResponse from(RideView v) {
        return new RideResponse(
                v.rideId(),
                v.status(),
                v.quotedFare(),
                v.finalFare(),
                v.currency()
        );
    }

    public static RideResponse from(Ride r) {
        return new RideResponse(
                r.getRideId(),
                r.getStatus(),
                r.getQuotedFare(),
                r.getFinalFare(),
                r.getCurrency()
        );
    }
}
