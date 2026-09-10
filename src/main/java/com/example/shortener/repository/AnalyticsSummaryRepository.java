package com.example.shortener.repository;

import com.example.shortener.domain.AnalyticsSummary;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, String> {
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO analytics_summary(short_code, access_count, last_accessed_at)
        VALUES (:code, 1, :ts)
        ON CONFLICT (short_code) DO UPDATE
        SET access_count = analytics_summary.access_count + 1,
            last_accessed_at = GREATEST(analytics_summary.last_accessed_at, EXCLUDED.last_accessed_at)
        """, nativeQuery = true)
    void increment(@Param("code") String code, @Param("ts") Instant ts);
}
