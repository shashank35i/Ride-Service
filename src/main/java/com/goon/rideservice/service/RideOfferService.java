package com.goon.rideservice.service;

import com.goon.rideservice.dto.*;
import com.goon.rideservice.dto.request.AcceptOfferRequest;
import com.goon.rideservice.dto.response.AcceptOfferResponse;
import com.goon.rideservice.entity.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;
    private final RideRepository rideRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public UUID declineOffer(UUID offerId, UUID driverId) {

        int updated = rideOfferRepository.declineOffer(offerId, driverId);

        if (updated == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid offer or already declined"
            );
        }

        return rideOfferRepository.findById(offerId)
                .orElseThrow()
                .getRideId();
    }

    @Transactional
    public AcceptOfferResponse acceptOffer(
            UUID offerId,
            AcceptOfferRequest request) {

        RideOffer offer = rideOfferRepository.findByIdForUpdate(offerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Offer not found"));

        Ride ride = rideRepository.findByIdForUpdate(offer.getRideId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found"));

        if (request == null || request.driverId() == null
                || !offer.getDriverId().equals(request.driverId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Invalid driver or offer");
        }

        if (offer.getStatus() != RideOfferStatus.OFFERED
                || !offer.getExpiresAt().isAfter(Instant.now())
                || ride.getStatus() != RideStatus.REQUESTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Offer unavailable");
        }

        offer.setStatus(RideOfferStatus.ACCEPTED);
        ride.setStatus(RideStatus.ASSIGNED);
        ride.setDriverId(request.driverId());

        UUID rideId = ride.getRideId();

        DriverAssignedPayload payload = DriverAssignedPayload.builder()
                .rideId(rideId)
                .customerId(ride.getCustomerId())
                .driverId(request.driverId())
                .driverName(request.driverName())
                .vehicleModel(request.vehicleModel())
                .vehicleNumber(request.vehicleNumber())
                .build();

        outboxRepository.save(Outbox.builder()
                .aggregateId(rideId)
                .eventType("DRIVER_ASSIGNED")
                .payload(JsonMapper.shared().writeValueAsString(payload))
                .build());

        return new AcceptOfferResponse(rideId, RideStatus.ASSIGNED);
    }

    @Transactional
    public UUID expireOffer(UUID offerId) {

        RideOffer offer = rideOfferRepository.findByIdForUpdate(offerId)
                .orElse(null);

        if (offer == null) {
            return null;
        }

        Ride ride = rideRepository.findByIdForUpdate(offer.getRideId())
                .orElseThrow();

        if (ride.getStatus() != RideStatus.REQUESTED
                || offer.getStatus() != RideOfferStatus.OFFERED
                || offer.getExpiresAt().isAfter(Instant.now())) {
            return null;
        }

        offer.setStatus(RideOfferStatus.EXPIRED);

        return ride.getRideId();
    }
}