package com.example.shortener.repository;

import com.example.shortener.domain.ShortUrlEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ShortUrlRepository extends JpaRepository<ShortUrlEntity, UUID> {
    Optional<ShortUrlEntity> findByShortCode(String shortCode);
    Optional<ShortUrlEntity> findByUrlFingerprint(String urlFingerprint);

    @Modifying
    @Transactional
    @Query(value = """
      INSERT INTO short_url(id, short_code, original_url, normalized_url, url_fingerprint, created_at, expires_at, status, version)
      VALUES (:id, :code, :originalUrl, :normalizedUrl, :fingerprint, :createdAt, :expiresAt, 'ACTIVE', 0)
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id,
                       @Param("code") String code,
                       @Param("originalUrl") String originalUrl,
                       @Param("normalizedUrl") String normalizedUrl,
                       @Param("fingerprint") String fingerprint,
                       @Param("createdAt") Instant createdAt,
                       @Param("expiresAt") Instant expiresAt);
}
