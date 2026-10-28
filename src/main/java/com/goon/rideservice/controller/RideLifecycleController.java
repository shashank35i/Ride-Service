package com.goon.rideservice.controller;


import com.goon.rideservice.dto.StartResult;
import com.goon.rideservice.dto.request.StartRequest;
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
    private final com.goon.rideservice.service.FareFinalizationService fareFinalizationService;

    @PostMapping("/{rideId}/end")
    public ResponseEntity<com.goon.rideservice.dto.EndRideResponse> end(
            @PathVariable UUID rideId, @RequestBody UUID driverId) {
        rideLifecycleService.endRide(rideId, driverId);
        var result = fareFinalizationService.finalizeFare(rideId);
        return ResponseEntity.status(result.finalFare() == null ? 202 : 200).body(result);
    }

    @PostMapping("/{rideId}/arrived")
    public ResponseEntity<Instant> markArrived(
            @PathVariable UUID rideId,
            @RequestBody UUID driverId
            ){

        Instant res = rideLifecycleService.markDriverArrived(rideId,driverId);
        return ResponseEntity.ok(res);
    }


    @PostMapping("/{rideId}/start")
    public ResponseEntity<StartResult> start(
            @PathVariable UUID rideId,
            @RequestBody StartRequest request
    ){
        StartResult res = rideLifecycleService.verifyStartOtp(rideId,request.driverId(), request.otp());

        return ResponseEntity.status(
                res.success() ? 200 : res.status().equals("OTP_LOCKED") ? 429 : 400)
                .body(res);
    }  ///429 -> Too many incorrect OTP attempts
    ///400 -> incrct otp
}
