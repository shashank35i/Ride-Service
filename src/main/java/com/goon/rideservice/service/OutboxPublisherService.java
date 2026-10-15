package com.goon.rideservice.service;

import com.goon.rideservice.entity.OutboxEvent;
import com.goon.rideservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void publishBatch() throws Exception {

        var events = outboxRepository.fetchBatch(100);

        for (OutboxEvent event : events) {

            String topic = switch (event.getEventType()) {
                case "RIDE_REQUESTED" -> "ride-requested";
                default -> throw new IllegalStateException(
                        "Unknown event: " + event.getEventType());
            };

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(
                            topic,
                            event.getAggregateId().toString(),
                            event.getPayload()
                    );

            record.headers().add(
                    "eventId",
                    event.getEventId().toString()
                            .getBytes(StandardCharsets.UTF_8)
            );

            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

            event.setPublished(true);
            event.setPublishedAt(Instant.now());
        }
    }
}