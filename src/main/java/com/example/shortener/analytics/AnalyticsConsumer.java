package com.example.shortener.analytics;

import com.example.shortener.domain.ProcessedAnalyticsEvent;
import com.example.shortener.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Component
public class AnalyticsConsumer {
    private final ObjectMapper mapper; private final ProcessedAnalyticsEventRepository processed;
    private final AnalyticsSummaryRepository summary; private final AnalyticsGeoRepository geo; private final Clock clock;
    public AnalyticsConsumer(ObjectMapper mapper,ProcessedAnalyticsEventRepository processed,AnalyticsSummaryRepository summary,AnalyticsGeoRepository geo,Clock clock){
        this.mapper=mapper;this.processed=processed;this.summary=summary;this.geo=geo;this.clock=clock;
    }

    @KafkaListener(topics="url-access-events", groupId="url-analytics")
    @Transactional
    public void consume(String json) throws Exception {
        AnalyticsEvent e=mapper.readValue(json,AnalyticsEvent.class);
        try { processed.saveAndFlush(new ProcessedAnalyticsEvent(e.eventId(),clock.instant())); }
        catch (DataIntegrityViolationException duplicate) { return; }
        summary.increment(e.shortCode(),e.accessedAt());
        String country=(e.country()==null||e.country().isBlank())?"UNKNOWN":e.country();
        String region=(e.region()==null||e.region().isBlank())?"UNKNOWN":e.region();
        geo.increment(e.shortCode(),country,region);
    }
}
