package com.goon.rideservice.worker;

import com.goon.rideservice.dto.PaymentResultPayload;
import com.goon.rideservice.service.RidePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.*;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RidePaymentWorker {
    private final RidePaymentService payments;

    @KafkaListener(topics = {"payment-completed", "payment-failed"}, groupId = "ride-payment-status",
            containerFactory = "paymentStatusKafkaListenerContainerFactory")
    public void consume(String body, @Header("eventId") String eventId,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Acknowledgment ack) {
        payments.apply(UUID.fromString(eventId), topic,
                JsonMapper.shared().readValue(body, PaymentResultPayload.class));
        ack.acknowledge();
    }
}

