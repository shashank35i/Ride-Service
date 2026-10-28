package com.goon.rideservice.dto;

import com.goon.rideservice.entity.*;
import java.math.BigDecimal;
import java.util.UUID;

public record EndRideResponse(UUID rideId, RideStatus status, PaymentStatus paymentStatus,
                              BigDecimal finalFare, String currency) {
    public static EndRideResponse from(Ride ride) {
        return new EndRideResponse(ride.getRideId(), ride.getStatus(),
                ride.getPaymentStatus(), ride.getFinalFare(), ride.getCurrency());
    }
}

