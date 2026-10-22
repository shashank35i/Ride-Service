package com.goon.rideservice.dto.response;

import com.goon.rideservice.entity.RideStatus;

import java.util.UUID;

public record AcceptOfferResponse(
        UUID rideId,
        RideStatus status
) {}