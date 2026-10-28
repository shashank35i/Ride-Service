package com.goon.rideservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "event_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "aggregate_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID aggregateId;   //rideid

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;
}
