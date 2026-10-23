package com.goon.rideservice.worker;

import com.goon.rideservice.dto.RideRequestedPayload;
import com.goon.rideservice.service.RideMatchingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideRequestedDltWorker {

    private final RideMatchingService matchingService;

    @KafkaListener(
            topics = "ride-requested-dlt",
            groupId = "ride-matching-dlt",
            containerFactory = "rideDltKafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment ack
    ) throws Exception {

        Header header = record.headers().lastHeader("eventId");

        if (header == null) {
            throw new IllegalStateException("Missing eventId");
        }

        UUID eventId = UUID.fromString(
                new String(header.value(), StandardCharsets.UTF_8)
        );

        RideRequestedPayload event = JsonMapper.shared()
                .readValue(record.value(), RideRequestedPayload.class);

        matchingService.process(eventId, event);

        ack.acknowledge();

        log.info("DLT recovered ride {}", event.rideId());
    }
}