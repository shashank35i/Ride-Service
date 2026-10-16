package com.goon.rideservice.service;


import com.goon.rideservice.entity.*;
import com.goon.rideservice.dto.RideOfferedPayload;
import com.goon.rideservice.dto.RideRequestedPayload;
import com.goon.rideservice.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideMatchingService {

    private final ProcessedEventRepository processedRepository;
    private final RideOfferRepository offerRepository;
    private final OutboxRepository outboxRepository;
    private final RideRepository rideRepository;

    @Transactional
    public void process(
            UUID eventId,
            RideRequestedPayload event,
            List<String> drivers) {

        if (processedRepository.existsById(eventId)) {
            return;
        }

        if (drivers.isEmpty()) {
            throw new IllegalStateException(
                    "No nearby drivers for ride " + event.rideId());
        }

        var ride = rideRepository.findById(event.rideId())
                .orElseThrow(() ->
                        new IllegalStateException("Ride not found"));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            processedRepository.save(
                    new ProcessedEvent(eventId, Instant.now()));
            return;
        }

        RideOffer offer = RideOffer.builder()
                .rideId(event.rideId())
                .driverId(drivers.get(0))
                .expiresAt(Instant.now().plusSeconds(20))
                .build();

        offerRepository.saveAndFlush(offer);

        RideOfferedPayload payload = new RideOfferedPayload(
                offer.getOfferId(),
                offer.getRideId(),
                offer.getDriverId()
        );

        try {
            outboxRepository.save(
                    Outbox.builder()
                            .aggregateId(event.rideId())
                            .eventType("RIDE_OFFERED")
                            .payload(JsonMapper.shared()
                                    .writeValueAsString(payload))
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize ride offer", e);
        }

        processedRepository.saveAndFlush(
                new ProcessedEvent(eventId, Instant.now())
        );
    }
}