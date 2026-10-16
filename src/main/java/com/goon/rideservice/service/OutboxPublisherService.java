package com.goon.rideservice.service;

import com.goon.rideservice.entity.Outbox;
import com.goon.rideservice.entity.OutboxStatus;
import com.goon.rideservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void publishBatch() throws Exception {

        var events = outboxRepository.fetchBatch(100);

        for (Outbox event : events) {

            String topic = switch (event.getEventType()) {
                case "RIDE_REQUESTED" -> "ride-requested";
                case "RIDE_OFFERED" -> "ride-offered";
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
                            .getBytes(UTF_8)
            );

            kafkaTemplate.send(record).get(10,TimeUnit.SECONDS);

            /**
             *
             * if .get throws exception , loop exits entirely and rollbacks transaction
             * if no exception , then commits transaction of updating status
             */

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
        }
    }
}