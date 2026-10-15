package com.goon.rideservice.worker;

import com.goon.rideservice.service.OutboxPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherWorker {

    private final OutboxPublisherService publisherService;

    @Scheduled(fixedDelayString = "${app.outbox.delay-ms:500}")
    public void publish() {
        try {
            publisherService.publishBatch();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Outbox publisher interrupted", e);
        } catch (Exception e) {
            log.error("Outbox publishing failed", e);
        }
    }
}