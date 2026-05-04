package com.shally.urlshortener.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.repository.UrlRepository;

@Service
public class ClickConsumerService {

    // ✅ Thread-safe map
    private final Map<String, Long> clickBuffer = new ConcurrentHashMap<>();

    @Autowired
    private UrlRepository urlRepository;

    @KafkaListener(topics = "url-events", groupId = "url-shortener-group")
    public void consume(String shortCode) {

        clickBuffer.merge(shortCode, 1L, Long::sum);

        System.out.println("Buffered click for: " + shortCode);
    }

    @Scheduled(fixedRate = 10000)
    public void flushToDB() {

        // ✅ Avoid unnecessary DB calls
        if (clickBuffer.isEmpty()) {
            return;
        }

        System.out.println("Flushing to DB: " + clickBuffer);

        clickBuffer.forEach((shortCode, count) -> {
            urlRepository.incrementClickCount(shortCode, count);
        });

        clickBuffer.clear();
    }
}