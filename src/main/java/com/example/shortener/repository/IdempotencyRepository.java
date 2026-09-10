package com.example.shortener.repository;

import com.example.shortener.domain.IdempotencyRecord;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {
    @Modifying
    @Transactional
    @Query(value = """
      INSERT INTO idempotency_record(idempotency_key, request_hash, short_code, created_at)
      VALUES (:key, :requestHash, :shortCode, :createdAt)
      ON CONFLICT DO NOTHING
      """, nativeQuery = true)
    int insertIfAbsent(@Param("key") String key,
                       @Param("requestHash") String requestHash,
                       @Param("shortCode") String shortCode,
                       @Param("createdAt") Instant createdAt);
}
