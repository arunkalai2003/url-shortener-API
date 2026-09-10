package com.example.shortener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord {
    @Id
    @Column(name = "idempotency_key", length = 128)
    private String key;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "short_code", nullable = false, length = 32)
    private String shortCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IdempotencyRecord() {}
    public IdempotencyRecord(String key, String requestHash, String shortCode, Instant createdAt) {
        this.key = key; this.requestHash = requestHash; this.shortCode = shortCode; this.createdAt = createdAt;
    }
    public String getKey() { return key; }
    public String getRequestHash() { return requestHash; }
    public String getShortCode() { return shortCode; }
}
