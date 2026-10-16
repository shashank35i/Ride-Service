package com.goon.rideservice.dto;

import java.util.UUID;

public record RideOfferedPayload(
        UUID offerId,
        UUID rideId,
        UUID driverId
) {}