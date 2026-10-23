package com.goon.rideservice.dto;


import lombok.Builder;
import java.util.UUID;

@Builder
public record DriverAssignedPayload(
        UUID rideId,
        UUID customerId,
        UUID driverId,
        String startOtp,
        String driverName,
        String vehicleModel,
        String vehicleNumber
) {}