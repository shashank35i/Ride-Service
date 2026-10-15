package com.goon.rideservice.dto.request;

import com.goon.rideservice.service.VehicleType;

public record RideRequest(
        double pickupLat,
        double pickupLon,
        double dropLat,
        double dropLon,
        VehicleType vehicleType,
        String signedQuote
) {
}
