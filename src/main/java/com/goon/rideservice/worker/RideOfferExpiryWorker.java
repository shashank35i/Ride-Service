package com.goon.rideservice.worker;

import com.goon.rideservice.repository.RideOfferRepository;
import com.goon.rideservice.service.RideMatchingService;
import com.goon.rideservice.service.RideOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideOfferExpiryWorker {

    private final RideOfferRepository offerRepository;
    private final RideOfferService offerService;
    private final RideMatchingService matchingService;

    @Scheduled(fixedDelay = 1000)
    public void expireOffers() {

        var offerIds = offerRepository.findExpiredOfferIds(
                Instant.now(),
                PageRequest.of(0, 100)
        );

        for (UUID offerId : offerIds) {
            try {
                UUID rideId = offerService.expireOffer(offerId);

                if (rideId != null) {
                    matchingService.offerNextDriver(rideId);
                }

            } catch (Exception e) {
                log.error("Failed to expire offer {}", offerId, e);
            }
        }
    }
}