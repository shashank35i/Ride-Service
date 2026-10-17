package com.goon.rideservice.controller;


import com.goon.rideservice.service.RideOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/offers")
@RequiredArgsConstructor
public class RideOfferController {
    private  final RideOfferService rideOfferService;

    @PatchMapping("/{offerId}/decline")
    public ResponseEntity<Void> decline(
        @PathVariable UUID offerId,
        @RequestBody UUID driverId){

        rideOfferService.declineOffer(offerId,driverId);
        return ResponseEntity.noContent().build();
}

}
