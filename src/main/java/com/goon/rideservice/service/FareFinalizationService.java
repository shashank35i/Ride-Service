package com.goon.rideservice.service;

import com.goon.grpc.pricing.*;
import com.goon.rideservice.dto.*;
import com.goon.rideservice.entity.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FareFinalizationService {
    private final RideRepository rides;
    private final OutboxRepository outbox;
    private final PricingServiceGrpc.PricingServiceBlockingStub pricing;

    @Transactional
    public EndRideResponse finalizeFare(UUID rideId) {
        Ride ride = rides.findByIdForUpdate(rideId).orElseThrow();
        if (ride.getStatus() != RideStatus.ENDED
                || ride.getPaymentStatus() != PaymentStatus.FARE_PENDING) {
            return EndRideResponse.from(ride);
        }
        ride.setFareAttempts(Math.addExact(ride.getFareAttempts(), 1));
        FinalFareResponse result;
        try {
            result = pricing.withDeadlineAfter(5, TimeUnit.SECONDS).calculateFinalFare(
                    FinalFareRequest.newBuilder().setRideId(rideId.toString())
                            .setQuotedFarePaise(ride.getQuotedFare().movePointRight(2).longValueExact())
                            .setWaitingTimeSeconds(ride.getWaitingTimeS())
                            .setVehicleType(ride.getVehicleType().name()).build());
            if (result.getFinalFarePaise() < 0 || !"INR".equals(result.getCurrency())) {
                throw new IllegalStateException("Invalid pricing response");
            }
        } catch (RuntimeException e) {
            long delay = Math.min(300, 5L << Math.min(ride.getFareAttempts(), 6));
            ride.setNextFareAttemptAt(Instant.now().plusSeconds(delay));
            log.warn("Final fare pending for ride {}; attempt {} failed ({})",
                    rideId, ride.getFareAttempts(), e.getClass().getSimpleName());
            return EndRideResponse.from(ride);
        }
        ride.setFinalFare(BigDecimal.valueOf(result.getFinalFarePaise(), 2));
        ride.setFareSource("PRICING");
        ride.setPaymentStatus(PaymentStatus.PAYMENT_PENDING);
        ride.setNextFareAttemptAt(null);
        var event = new FareFinalizedPayload(rideId, ride.getCustomerId(), ride.getDriverId(),
                result.getFinalFarePaise(), result.getCurrency(), Instant.now());
        outbox.save(Outbox.builder().aggregateId(rideId).eventType("FARE_FINALIZED")
                .payload(JsonMapper.shared().writeValueAsString(event)).build());
        return EndRideResponse.from(ride);
    }
}

