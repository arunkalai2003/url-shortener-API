package com.example.shortener.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_analytics_event")
public class ProcessedAnalyticsEvent {
    @Id private UUID eventId;
    @Column(nullable = false) private Instant processedAt;
    public ProcessedAnalyticsEvent() {}
    public ProcessedAnalyticsEvent(UUID eventId, Instant processedAt) { this.eventId = eventId; this.processedAt = processedAt; }
}
