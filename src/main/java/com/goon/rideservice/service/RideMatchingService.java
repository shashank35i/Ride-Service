package com.goon.rideservice.service;

import com.goon.rideservice.entity.*;
import com.goon.rideservice.dto.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RideMatchingService {

    private final RideRepository rideRepository;
    private final RideOfferRepository offerRepository;
    private final ProcessedEventRepository processedRepository;
    private final OutboxRepository outboxRepository;
    private final NearbyDriverService nearbyDriverService;

    @Transactional
    public void process(UUID eventId, RideRequestedPayload event) {

        if (processedRepository.existsById(eventId)) {
            return;
        }

        Ride ride = rideRepository.findById(event.rideId())
                .orElseThrow(() ->
                        new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            processedRepository.save(
                    new ProcessedEvent(eventId, Instant.now()));
            return;
        }

        Set<UUID> excludedDrivers =
                offerRepository.findOfferedDriverIds(event.rideId());

        Optional<UUID> driver = nearbyDriverService.findNearby(
                event.pickupLat(),
                event.pickupLon(),
                event.vehicleType(),
                excludedDrivers
        );

        if (driver.isEmpty()) {
            throw new IllegalStateException("No eligible drivers found");
        }

        RideOffer offer = RideOffer.builder()
                .rideId(event.rideId())
                .driverId(driver.get())
                .expiresAt(Instant.now().plusSeconds(20))
                .build();

        offerRepository.saveAndFlush(offer);

        RideOfferedPayload payload = new RideOfferedPayload(
                offer.getOfferId(),
                offer.getRideId(),
                offer.getDriverId()
        );

        outboxRepository.save(
                Outbox.builder()
                        .aggregateId(event.rideId())
                        .eventType("RIDE_OFFERED")
                        .payload(JsonMapper.shared()
                                .writeValueAsString(payload))
                        .build()
        );

        processedRepository.save(
                new ProcessedEvent(eventId, Instant.now())
        );
    }
}