package com.goon.rideservice.service;

import com.goon.rideservice.entity.*;
import com.goon.rideservice.dto.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideMatchingService {

    private final RideRepository rideRepository;
    private final RideOfferRepository offerRepository;
    private final ProcessedEventRepository processedRepository;
    private final OutboxRepository outboxRepository;
    private final NearbyDriverService nearbyDriverService;

    @Transactional
    public void process(UUID eventId, RideRequestedPayload event) {

        Ride ride = rideRepository.findByIdForUpdate(event.rideId())
                .orElseThrow(() ->
                        new IllegalStateException("Ride not found"));

        if (processedRepository.existsById(eventId)) {
            return;
        }

        createNextOffer(ride);

        processedRepository.saveAndFlush(
                new ProcessedEvent(eventId, Instant.now())
        );
    }

    @Transactional
    public void offerNextDriver(UUID rideId) {

        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new IllegalStateException("Ride not found"));

        createNextOffer(ride);
    }

    private void createNextOffer(Ride ride) {

        UUID rideId = ride.getRideId();

        if (ride.getStatus() != RideStatus.REQUESTED) {
            return;
        }

        if (offerRepository.existsByRideIdAndStatus(
                rideId, RideOfferStatus.OFFERED)) {
            return;
        }

        Set<UUID> excludedDrivers =
                offerRepository.findOfferedDriverIds(rideId);

        Optional<UUID> driver = nearbyDriverService.findNearby(
                ride.getPickupLat(),
                ride.getPickupLon(),
                ride.getVehicleType().name(),
                excludedDrivers
        );

        if (driver.isEmpty()) {
            log.info("No eligible drivers found for ride {}", rideId);
            return;
        }

        Instant expiresAt = Instant.now().plusSeconds(15);

        RideOffer offer = RideOffer.builder()
                .rideId(rideId)
                .driverId(driver.get())
                .status(RideOfferStatus.OFFERED)
                .expiresAt(expiresAt)
                .build();

        offerRepository.saveAndFlush(offer);

        RideOfferedPayload payload = RideOfferedPayload.builder()
                .offerId(offer.getOfferId())
                .rideId(rideId)
                .driverId(offer.getDriverId())
                .pickupLat(ride.getPickupLat())
                .pickupLon(ride.getPickupLon())
                .dropLat(ride.getDropLat())
                .dropLon(ride.getDropLon())
                .estimatedFare(ride.getQuotedFare())
                .currency(ride.getCurrency())
                .expiresAt(expiresAt)
                .expiresInSeconds(15)
                .build();

        outboxRepository.save(
                Outbox.builder()
                        .aggregateId(rideId)
                        .eventType("RIDE_OFFERED")
                        .payload(JsonMapper.shared()
                                .writeValueAsString(payload))
                        .build()
        );
    }
}