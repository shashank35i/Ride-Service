package com.goon.rideservice.worker;

import com.goon.rideservice.repository.RideRepository;
import com.goon.rideservice.service.FareFinalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinalFareRetryWorker {
    private final RideRepository rides;
    private final FareFinalizationService fares;

    @Scheduled(fixedDelayString = "${app.fare.retry-delay-ms:5000}")
    public void retryPendingFares() {
        for (var id : rides.findFaresDue(Instant.now(), PageRequest.of(0, 25))) {
            try {
                fares.finalizeFare(id);
            } catch (RuntimeException e) {
                log.warn("Final fare retry deferred for ride {} ({})", id, e.getClass().getSimpleName());
            }
        }
    }
}

