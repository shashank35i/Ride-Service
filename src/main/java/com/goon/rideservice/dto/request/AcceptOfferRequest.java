package com.goon.rideservice.dto.request;

import java.util.UUID;

public record AcceptOfferRequest(
        UUID driverId,
        String driverName,
        String vehicleModel,
        String vehicleNumber
) {}