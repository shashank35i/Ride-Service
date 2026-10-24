package com.goon.rideservice.worker;

import com.goon.rideservice.dto.RideRequestedPayload;
import com.goon.rideservice.service.RideMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Header;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RideMatchingWorker {

    private final RideMatchingService rideMatchingService;

    @KafkaListener(
            topics = "ride-requested",
            groupId = "ride-matching",
            containerFactory = "rideMatchingKafkaListenerContainerFactory"
    )
    public void consume(
            String message,
            @Header("eventId") String eventId,
            Acknowledgment ack) {

        RideRequestedPayload event = JsonMapper.shared()
                .readValue(message, RideRequestedPayload.class);

        rideMatchingService.process(
                UUID.fromString(eventId),
                event
        );

        ack.acknowledge();
    }
}