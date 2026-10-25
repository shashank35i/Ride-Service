package com.goon.rideservice.dto.request;

import java.util.UUID;

public record StartRequest(
        UUID driverId,
        String otp

) {
}
