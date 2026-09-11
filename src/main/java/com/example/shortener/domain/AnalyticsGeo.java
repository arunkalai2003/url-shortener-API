package com.example.shortener.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "analytics_geo")
@IdClass(AnalyticsGeoKey.class)
public class AnalyticsGeo {
    @Id
    @Column(name = "short_code", length = 32)
    private String shortCode;
    @Id
    @Column(length = 8)
    private String country;
    @Id
    @Column(length = 64)
    private String region;
    @Column(name = "access_count", nullable = false)
    private long accessCount;

    public AnalyticsGeo() {
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public long getAccessCount() {
        return accessCount;
    }
}
