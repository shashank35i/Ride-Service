package com.goon.rideservice.service;

import com.goon.rideservice.dto.PaymentResultPayload;
import com.goon.rideservice.entity.*;
import com.goon.rideservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RidePaymentService {
    private final RideRepository rides;
    private final ProcessedEventRepository processed;

    @Transactional
    public void apply(UUID eventId, String topic, PaymentResultPayload event) {
        Ride ride = rides.findByIdForUpdate(event.rideId()).orElseThrow();
        if (processed.existsById(eventId)) {
            return;
        }
        String expected = "payment-completed".equals(topic) ? "PAID" : "FAILED";
        if (!expected.equals(event.status()) || event.revision() < 1
                || event.paymentId() == null || event.occurredAt() == null
                || !ride.getCustomerId().equals(event.customerId())
                || ride.getStatus() != RideStatus.ENDED || ride.getFinalFare() == null
                || ride.getFinalFare().movePointRight(2).longValueExact() != event.amountMinor()
                || !ride.getCurrency().equals(event.currency())) {
            throw new IllegalArgumentException("Payment event does not match finalized ride");
        }
        if (ride.getPaymentStatus() != PaymentStatus.PAID
                && ("PAID".equals(event.status()) || event.revision() > ride.getPaymentRevision())) {
            ride.setPaymentStatus("PAID".equals(event.status())
                    ? PaymentStatus.PAID : PaymentStatus.PAYMENT_FAILED);
            ride.setPaymentRevision(Math.max(ride.getPaymentRevision(), event.revision()));
        }
        processed.saveAndFlush(new ProcessedEvent(eventId, Instant.now()));
    }
}

