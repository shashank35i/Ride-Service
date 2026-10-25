package com.goon.rideservice.service;

import com.goon.rideservice.dto.StartResult;
import com.goon.rideservice.entity.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideLifecycleService {

    private final RideRepository rideRepository;
    private final OutboxRepository outboxRepository;
    private final StringRedisTemplate redis;

    private static final double MAX_DISTANCE_M = 100;
    private static final long MAX_GPS_AGE_MS = 15000;
    private static final int MAX_OTP_ATTEMPTS = 5;

    @Transactional
    public Instant markDriverArrived(UUID rideId, UUID driverId) {

        Ride ride = getRide(rideId, driverId);

        if (ride.getStatus() == RideStatus.DRIVER_ARRIVED) {
            return ride.getArrivedAt();
        }

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Ride is not assigned");
        }

        String id = driverId.toString();

        Double lastSeen = redis.opsForZSet()
                .score("drivers:last_seen", id);

        if (lastSeen == null ||
                System.currentTimeMillis() - lastSeen > MAX_GPS_AGE_MS ||
                lastSeen > System.currentTimeMillis() + 2000) {
            throw new IllegalStateException("GPS location is outdated");
        }

        List<Point> positions = redis.opsForGeo()    ///spring redis always returns list of points even when stored single point .
                .position("loc:" + rideId, id);

        if (positions == null || positions.isEmpty()
                || positions.get(0) == null) {
            throw new IllegalStateException("Driver location unavailable");
        }

        Point p = positions.get(0);

        double distance = distanceMeters(
                p.getY(), p.getX(),
                ride.getPickupLat(), ride.getPickupLon()
        );

        if (distance > MAX_DISTANCE_M) {
            throw new IllegalStateException(
                    "Move closer to pickup before marking arrived"
            );
        }

        Instant now = Instant.now();

        ride.setStatus(RideStatus.DRIVER_ARRIVED);
        ride.setArrivedAt(now);

        publish(rideId, "DRIVER_ARRIVED", Map.of(
                "rideId", rideId.toString(),
                "driverId", id,
                "arrivedAt", now.toString()
        ));

        return now;
    }

    @Transactional
    public StartResult verifyStartOtp(
            UUID rideId, UUID driverId, String otp
    ) {

        Ride ride = getRide(rideId, driverId);

        if (ride.getStatus() == RideStatus.STARTED) {
            return new StartResult(true, "ALREADY_STARTED",
                    ride.getStartedAt());
        }

        if (ride.getStatus() != RideStatus.DRIVER_ARRIVED) {
            throw new IllegalStateException("Driver has not arrived");
        }

        if (ride.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            return new StartResult(false, "OTP_LOCKED", null);
        }

        if (otp == null || !otp.equals(ride.getStartOtp())) {
            ride.setOtpAttempts(ride.getOtpAttempts() + 1);

            return new StartResult(
                    false,
                    ride.getOtpAttempts() >= MAX_OTP_ATTEMPTS
                            ? "OTP_LOCKED" : "INVALID_OTP",
                    null
            );
        }

        Instant now = Instant.now();

        ride.setStartedAt(now);
        ride.setStatus(RideStatus.STARTED);

        ride.setWaitingTimeS(
                Math.toIntExact(
                        Duration.between(
                                ride.getArrivedAt(), now
                        ).getSeconds()
                )
        );

        publish(rideId, "RIDE_STARTED", Map.of(
                "rideId", rideId.toString(),
                "driverId", driverId.toString(),
                "startedAt", now.toString()
        ));

        return new StartResult(true, "STARTED", now);
    }

    private Ride getRide(UUID rideId, UUID driverId) {

        Ride ride = rideRepository.findByIdForUpdate(rideId)
                .orElseThrow(() ->
                        new IllegalStateException("Ride not found"));

        if (!driverId.equals(ride.getDriverId())) {
            throw new SecurityException("Driver not assigned to this ride");
        }

        return ride;
    }

    private void publish(
            UUID rideId, String type, Map<String, String> data
    ) {

        outboxRepository.save(
                Outbox.builder()
                        .aggregateId(rideId)
                        .eventType(type)
                        .payload(JsonMapper.shared()
                                .writeValueAsString(data))
                        .build()
        );
    }

    private double distanceMeters(double lat1, double lon1,
                                  double lat2, double lon2) {
        double a = Math.toRadians(lat2 - lat1);   //converts angle to radians
        double b = Math.toRadians(lon2 - lon1);

        double sinA = Math.sin(a / 2);
        double sinB = Math.sin(b / 2);

        double x = sinA * sinA                         ///main formula
                + Math.cos(Math.toRadians(lat1))     ///sinA^2 + cos(lat1) * cos(lat2) *sinB^2
                * Math.cos(Math.toRadians(lat2))
                * sinB * sinB;

        x = Math.min(1.0, Math.max(0.0, x));

        return 6371000 * 2 * Math.atan2(
                Math.sqrt(x), Math.sqrt(1 - x)
        );
    }

    /***
     * @Math.atan2 ->  angle(in radians) between the positive x-axis and
     * the ray pointing to the coordinate \((x, y)\)
     */

}