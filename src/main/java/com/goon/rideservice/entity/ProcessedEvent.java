package com.goon.rideservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private UUID eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

}
