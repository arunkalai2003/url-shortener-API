package com.example.shortener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "analytics_summary")
public class AnalyticsSummary {
    @Id
    @Column(name = "short_code", length = 32)
    private String shortCode;

    @Column(name = "access_count", nullable = false)
    private long accessCount;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    public AnalyticsSummary() {}
    public AnalyticsSummary(String shortCode) { this.shortCode = shortCode; }
    public String getShortCode() { return shortCode; }
    public long getAccessCount() { return accessCount; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
}
