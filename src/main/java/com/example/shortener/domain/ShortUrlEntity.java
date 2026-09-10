package com.example.shortener.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "short_url", uniqueConstraints = {
        @UniqueConstraint(name = "uk_short_url_code", columnNames = "short_code"),
        @UniqueConstraint(name = "uk_short_url_fingerprint", columnNames = "url_fingerprint")
})
public class ShortUrlEntity {
    @Id
    private UUID id;

    @Column(name = "short_code", nullable = false, length = 32)
    private String shortCode;

    @Column(name = "original_url", nullable = false, length = 4096)
    private String originalUrl;

    @Column(name = "normalized_url", nullable = false, length = 4096)
    private String normalizedUrl;

    @Column(name = "url_fingerprint", nullable = false, length = 64)
    private String urlFingerprint;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UrlStatus status;

    @Version
    private long version;

    public ShortUrlEntity() {}

    public static ShortUrlEntity create(String code, String originalUrl, String normalizedUrl,
                                        String fingerprint, Instant createdAt, Instant expiresAt) {
        ShortUrlEntity e = new ShortUrlEntity();
        e.id = UUID.randomUUID();
        e.shortCode = code;
        e.originalUrl = originalUrl;
        e.normalizedUrl = normalizedUrl;
        e.urlFingerprint = fingerprint;
        e.createdAt = createdAt;
        e.expiresAt = expiresAt;
        e.status = UrlStatus.ACTIVE;
        return e;
    }

    public UUID getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public String getNormalizedUrl() { return normalizedUrl; }
    public String getUrlFingerprint() { return urlFingerprint; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public UrlStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public void setStatus(UrlStatus status) { this.status = status; }
}
