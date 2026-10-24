package com.goon.rideservice.dto;

import java.time.Instant;

public record StartResult(
        boolean success,
        String status,
        Instant startedAt
) {}