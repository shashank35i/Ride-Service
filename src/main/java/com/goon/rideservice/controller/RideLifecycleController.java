package com.goon.rideservice.controller;


import com.goon.rideservice.service.RideLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/internal/rides")
@RequiredArgsConstructor
public class RideLifecycleController {

    private final RideLifecycleService rideLifecycleService;

    @PostMapping("/{rideId}/arrived")
    public ResponseEntity<Instant> markArrived(
            @PathVariable UUID rideId,
            @RequestBody UUID driverId
            ){

        Instant res = rideLifecycleService.markDriverArrived(rideId,driverId);
        return ResponseEntity.ok(res);
    }
}
