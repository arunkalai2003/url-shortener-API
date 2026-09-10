package com.example.shortener.repository;

import com.example.shortener.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AnalyticsGeoRepository extends JpaRepository<AnalyticsGeo, AnalyticsGeoKey> {
    List<AnalyticsGeo> findByShortCode(String shortCode);

    @Modifying @Transactional
    @Query(value = """
      INSERT INTO analytics_geo(short_code, country, region, access_count)
      VALUES (:code, :country, :region, 1)
      ON CONFLICT (short_code, country, region) DO UPDATE
      SET access_count = analytics_geo.access_count + 1
      """, nativeQuery = true)
    void increment(@Param("code") String code, @Param("country") String country, @Param("region") String region);
}
