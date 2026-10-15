package com.goon.rideservice.dto;

import com.goon.rideservice.entity.RideStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record RideView(UUID rideId, RideStatus status, BigDecimal quotedFare,
                       BigDecimal finalFare, String currency, String requestHash) {}

