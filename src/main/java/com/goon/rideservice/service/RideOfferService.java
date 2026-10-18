package com.goon.rideservice.service;

import com.goon.rideservice.repository.RideOfferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;

    @Transactional
    public UUID declineOffer(UUID offerId,UUID driverId){

        int status = rideOfferRepository.declineOffer(offerId,driverId);
        if(status==0){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,"invalid offer or already declined"
            );

        }

        return rideOfferRepository.findById(offerId)
                .orElseThrow()
                .getRideId();
    }
}
