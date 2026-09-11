package com.example.shortener.domain;

import java.io.Serializable;
import java.util.Objects;

public class AnalyticsGeoKey implements Serializable {
    private String shortCode;
    private String country;
    private String region;

    public AnalyticsGeoKey() {
    }

    public AnalyticsGeoKey(String shortCode, String country, String region) {
        this.shortCode = shortCode;
        this.country = country;
        this.region = region;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalyticsGeoKey k)) return false;
        return Objects.equals(shortCode, k.shortCode) && Objects.equals(country, k.country) && Objects.equals(region, k.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortCode, country, region);
    }
}
