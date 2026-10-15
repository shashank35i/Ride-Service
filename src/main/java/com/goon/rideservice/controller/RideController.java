package com.goon.rideservice.controller;

import com.goon.rideservice.service.RideService;
import com.goon.rideservice.dto.request.RideRequest;
import com.goon.rideservice.dto.response.RideResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;


    @PostMapping()
    public ResponseEntity<RideResponse> createRide(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                   @RequestHeader("X-User-Id") String customerId,
                                                   @RequestBody RideRequest rideRequest
    ){
        RideResponse response = rideService.createRide(idempotencyKey,customerId,rideRequest);
        return ResponseEntity
                .created(URI.create("/rides/"+response.rideId()))
                .body(response);
    }

}
