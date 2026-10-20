package com.goon.rideservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "event_id", columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

}
