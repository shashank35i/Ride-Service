package com.goon.rideservice.controller;


import com.goon.rideservice.dto.request.AcceptOfferRequest;
import com.goon.rideservice.dto.response.AcceptOfferResponse;
import com.goon.rideservice.service.RideMatchingService;
import com.goon.rideservice.service.RideOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/offers")
@RequiredArgsConstructor
public class RideOfferController {
    private  final RideMatchingService rideMatchingService;
    private final RideOfferService rideOfferService;

    @PatchMapping("/{offerId}/decline")
    public ResponseEntity<Void> decline(
        @PathVariable UUID offerId,
        @RequestBody UUID driverId){

        UUID rideId = rideOfferService.declineOffer(offerId,driverId);
        rideMatchingService.offerNextDriver(rideId);
        return ResponseEntity.noContent().build();
}

    @PostMapping("/{offerId}/accept")
    public ResponseEntity<AcceptOfferResponse> accept(
            @PathVariable UUID offerId,
            @RequestBody AcceptOfferRequest request) {

        AcceptOfferResponse response =
                rideOfferService.acceptOffer(offerId, request);

        return ResponseEntity.ok(response);
    }

}
