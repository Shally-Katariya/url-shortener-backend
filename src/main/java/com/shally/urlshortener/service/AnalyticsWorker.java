package com.shally.urlshortener.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsWorker {
    // Disabled: Redis KEYS command not supported on Upstash free tier
    // Click analytics handled by ClickConsumerService via Kafka
    @Scheduled(fixedRate = 60000)
    public void flushClickCounts() {
        // no-op
    }
}
