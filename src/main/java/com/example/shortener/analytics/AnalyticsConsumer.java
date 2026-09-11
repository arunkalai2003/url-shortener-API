package com.example.shortener.analytics;

import com.example.shortener.common.Constants;
import com.example.shortener.domain.ProcessedAnalyticsEvent;
import com.example.shortener.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;

/**
 * Kafka consumer that processes URL access events and updates analytics aggregates.
 * <p>
 * Listens to the {@code url-access-events} topic and, for each event, idempotently
 * records the event's processing (guarding against duplicate delivery), then updates
 * two aggregate views: an overall access {@link #summary} keyed by short code and
 * access time, and a {@link #geo} breakdown by country/region, with missing location
 * data normalized to {@link Constants#UNKNOWN}.
 * <p>
 * Deduplication relies on a unique constraint on the processed-event ID, so at-least-once
 * delivery semantics from Kafka do not result in double-counted analytics.
 *
 * @see AnalyticsEvent
 * @see ProcessedAnalyticsEventRepository
 */
@Component
public class AnalyticsConsumer {

    private final ObjectMapper mapper;
    private final ProcessedAnalyticsEventRepository processed;
    private final AnalyticsSummaryRepository summary;
    private final AnalyticsGeoRepository geo;
    private final Clock clock;

    public AnalyticsConsumer(ObjectMapper mapper, ProcessedAnalyticsEventRepository processed, AnalyticsSummaryRepository summary, AnalyticsGeoRepository geo, Clock clock) {
        this.mapper = mapper;
        this.processed = processed;
        this.summary = summary;
        this.geo = geo;
        this.clock = clock;
    }

    /**
     * Consumes a URL access event from Kafka, persists it idempotently, and updates
     * analytics aggregates (overall summary and geo breakdown).
     * <p>
     * Deduplicates events by {@code eventId}: if an event with the same ID has already
     * been processed (detected via a unique constraint violation on insert), this method
     * returns early without updating any aggregates, ensuring at-least-once delivery from
     * Kafka does not result in double-counting.
     *
     * @param inputPayload the raw JSON payload representing an {@link AnalyticsEvent}
     * @throws JsonProcessingException if {@code inputPayload} cannot be deserialized
     *                                  into an {@link AnalyticsEvent}
     */
    @KafkaListener(topics = "url-access-events", groupId = "url-analytics")
    @Transactional
    public void consume(String inputPayload) throws JsonProcessingException {
        AnalyticsEvent analyticsEvent = mapper.readValue(inputPayload, AnalyticsEvent.class);

        if (isDuplicate(analyticsEvent)) {
            return;
        }

        summary.increment(analyticsEvent.shortCode(), analyticsEvent.accessedAt());
        geo.increment(
                analyticsEvent.shortCode(),
                valueOrUnknown(analyticsEvent.country()),
                valueOrUnknown(analyticsEvent.region())
        );
    }

    /**
     * Records the given event as processed, returning whether it had already been
     * processed previously.
     *
     * @param analyticsEvent the event to check and record
     * @return {@code true} if this event's ID was already recorded (i.e. this is a
     *         duplicate delivery); {@code false} if it was newly recorded
     */
    private boolean isDuplicate(AnalyticsEvent analyticsEvent) {
        try {
            processed.saveAndFlush(new ProcessedAnalyticsEvent(analyticsEvent.eventId(), clock.instant()));
            return false;
        } catch (DataIntegrityViolationException duplicate) {
            return true;
        }
    }

    /**
     * Returns the given value, or {@link Constants#UNKNOWN} if it is {@code null} or blank.
     *
     * @param value the value to check
     * @return {@code value} if it has text, otherwise {@link Constants#UNKNOWN}
     */
    private static String valueOrUnknown(String value) {
        return StringUtils.hasText(value) ? value : Constants.UNKNOWN;
    }
}
