package com.example.shortener.repository;
import com.example.shortener.domain.ProcessedAnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ProcessedAnalyticsEventRepository extends JpaRepository<ProcessedAnalyticsEvent, UUID> {}
