package com.example.shortener.analytics;

public interface AnalyticsPublisher {

    void publish(AnalyticsEvent event);
}
