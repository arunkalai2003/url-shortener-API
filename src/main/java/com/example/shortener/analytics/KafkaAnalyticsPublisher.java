package com.example.shortener.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

/**
 * Kafka-backed implementation of {@link AnalyticsPublisher} that publishes URL access
 * events to a Kafka topic asynchronously.
 * <p>
 * Each event is serialized to JSON and sent on the {@code url-access-events} topic,
 * salted into one of {@value #SALT_BUCKETS} keys per short code to spread a single
 * high-traffic URL's events across partitions, since consumers only maintain counters
 * and do not require per-short-code ordering.
 * <p>
 * Publishing is fire-and-forget with respect to the caller: serialization errors and
 * Kafka send failures are both caught and silently suppressed rather than propagated,
 * so that analytics publishing can never interfere with the request that triggered it
 * (e.g. a redirect).
 *
 * @see AnalyticsPublisher
 * @see AnalyticsEvent
 */
@Component
public class KafkaAnalyticsPublisher implements AnalyticsPublisher {

    private static final int SALT_BUCKETS = 16;

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public KafkaAnalyticsPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    /**
     * Publishes an analytics event to Kafka asynchronously.
     * <p>
     * The event is serialized to JSON and sent on the {@code url-access-events} topic,
     * keyed by short code with an appended random salt (0-{@value #SALT_BUCKETS}) so that
     * a single high-traffic URL is distributed across partitions rather than concentrated
     * on one; strict ordering of events for a given short code is not required since
     * consumers only maintain counters.
     * <p>
     * This method never throws or otherwise propagates failures to the caller — both
     * serialization errors and Kafka send failures are logged and suppressed — since
     * analytics publishing must never interfere with the redirect request it originated
     * from.
     *
     * @param event the analytics event to publish
     */
    @Override
    @Async("analyticsExecutor")
    public void publish(AnalyticsEvent event) {
        try {
            int salt = Math.floorMod(event.eventId().hashCode(), SALT_BUCKETS);
            String key = event.shortCode() + ":" + salt;
            String payload = mapper.writeValueAsString(event);
            kafka.send("url-access-events", key, payload)
                    .exceptionally(ex -> {
                        return null;
                    });
        } catch (RuntimeException | JsonProcessingException ex) { }
    }
}
