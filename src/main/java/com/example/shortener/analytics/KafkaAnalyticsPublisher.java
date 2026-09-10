package com.example.shortener.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

@Component
public class KafkaAnalyticsPublisher implements AnalyticsPublisher {
    private final KafkaTemplate<String,String> kafka; private final ObjectMapper mapper;
    public KafkaAnalyticsPublisher(KafkaTemplate<String,String> kafka,ObjectMapper mapper){this.kafka=kafka;this.mapper=mapper;}
    @Override @Async("analyticsExecutor") public void publish(AnalyticsEvent event){
        try {
            // Salt event key so a single viral URL can distribute across Kafka partitions; ordering is not required for counters.
            int salt=Math.floorMod(event.eventId().hashCode(),16);
            kafka.send("url-access-events", event.shortCode()+":"+salt, mapper.writeValueAsString(event))
                    .exceptionally(ex -> null);
        } catch (RuntimeException | JsonProcessingException ignored) { /* analytics must never break redirect */ }
    }
}
