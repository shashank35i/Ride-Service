package com.goon.rideservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NearbyDriverService {

    private final StringRedisTemplate redisTemplate;

    public List<String> findNearby(
            double lat, double lon, String vehicleType) {

        String key = "drivers:available:" + vehicleType;

        var args = RedisGeoCommands.GeoSearchCommandArgs
                .newGeoSearchArgs()
                .sortAscending()
                .limit(20);

        var results = redisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(new Point(lon, lat)),
                new Distance(5, Metrics.KILOMETERS),
                args
        );

        if (results == null) {
            return List.of();
        }

        return results.getContent().stream()
                .map(r -> r.getContent().getName())
                .toList();
    }
}