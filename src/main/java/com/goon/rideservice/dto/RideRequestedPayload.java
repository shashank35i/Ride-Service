package com.goon.rideservice.dto;


import java.util.UUID;

public record RideRequestedPayload(
        UUID rideId,
        double pickupLat,
        double pickupLon,
        double dropLat,
        double dropLon,
        String vehicleType
) {}
