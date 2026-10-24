package com.goon.rideservice.service;


import com.goon.rideservice.dto.RideRequestedPayload;
import com.goon.rideservice.dto.RideView;
import com.goon.rideservice.dto.request.RideRequest;
import com.goon.rideservice.dto.response.RideResponse;
import com.goon.rideservice.entity.Outbox;
import com.goon.rideservice.entity.Ride;
import com.goon.rideservice.repository.OutboxRepository;
import com.goon.rideservice.repository.RideRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service

public class RideService {

    private final RideRepository rideRepository;
    private final SecretKey secretKey;
    private final OutboxRepository outboxRepository;

    public RideService(@Value("${app.secret-key}") String secretKey,
                       RideRepository rideRepository,
                       OutboxRepository outboxRepository
                       ){
        this.rideRepository = rideRepository;
        byte[] key = Base64.getDecoder().decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(key);
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public RideResponse createRide(
            String idempotencyKey,
            String customerId,
            RideRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Missing idempotency key");
        }

        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(request.signedQuote())
                    .getPayload();

        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid or expired quote");
        }

        String quoteId = claims.getId();

        if (quoteId == null || quoteId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Missing quote ID");
        }

        String hash = convertToHash(request, quoteId);

        Optional<RideView> existing =
                rideRepository.findByCustomerIdAndIdempotencyKey(
                        customerId, idempotencyKey);

        if (existing.isPresent()) {
            RideView view = existing.get();

            if (!view.requestHash().equals(hash)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key reused with different request");
            }

            return RideResponse.from(view);
        }

        if (!customerId.equals(claims.getSubject())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Customer mismatch");
        }

        if (claims.getExpiration() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Missing quote expiration");
        }

        if (Double.compare(request.pickupLat(), number(claims, "pickupLat")) != 0 ||
                Double.compare(request.pickupLon(), number(claims, "pickupLon")) != 0 ||
                Double.compare(request.dropLat(), number(claims, "dropLat")) != 0 ||
                Double.compare(request.dropLon(), number(claims, "dropLon")) != 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Quote coordinates mismatch");
        }

        if (!request.vehicleType().name()
                .equals(claims.get("vehicleType", String.class))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Vehicle type mismatch");
        }

        BigDecimal quotedFare;

        try {
            quotedFare = new BigDecimal(
                    claims.get("estimatedFare").toString());

        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid quoted fare");
        }

        if (quotedFare.signum() < 0 ||
                !"INR".equals(claims.get("currency", String.class))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid fare or currency");
        }

        Ride ride = Ride.builder()
                .customerId(UUID.fromString(customerId))
                .idempotencyKey(idempotencyKey)
                .requestHash(hash)
                .quoteId(quoteId)
                .pickupLat(request.pickupLat())
                .pickupLon(request.pickupLon())
                .dropLat(request.dropLat())
                .dropLon(request.dropLon())
                .vehicleType(request.vehicleType())
                .quotedFare(quotedFare)
                .currency("INR")
                .build();

        Ride saved = rideRepository.saveAndFlush(ride);

        RideRequestedPayload event = new RideRequestedPayload(
                saved.getRideId(),
                saved.getPickupLat(),
                saved.getPickupLon(),
                saved.getDropLat(),
                saved.getDropLon(),
                saved.getVehicleType().name()
        );

        String payload;

        try {
            payload = JsonMapper.shared().writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to serialize ride event", e);
        }

        outboxRepository.save(
                Outbox.builder()
                        .aggregateId(saved.getRideId())
                        .eventType("RIDE_REQUESTED")
                        .payload(payload)
                        .build()
        );

        return RideResponse.from(saved);
    }

    private double number(Claims claims, String name) {
        return ((Number) claims.get(name)).doubleValue();
    }

    private String convertToHash(RideRequest r, String quoteId) {
        String data = String.join("|",
                quoteId,
                BigDecimal.valueOf(r.pickupLat()).stripTrailingZeros().toPlainString(),
                BigDecimal.valueOf(r.pickupLon()).stripTrailingZeros().toPlainString(),
                BigDecimal.valueOf(r.dropLat()).stripTrailingZeros().toPlainString(),
                BigDecimal.valueOf(r.dropLon()).stripTrailingZeros().toPlainString(),
                r.vehicleType().name()
        );

        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(data.getBytes(UTF_8));

            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


}
