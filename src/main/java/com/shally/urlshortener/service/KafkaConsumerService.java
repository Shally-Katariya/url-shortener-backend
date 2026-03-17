package com.shally.urlshortener.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.repository.UrlRepository;

@Service
public class KafkaConsumerService {

    private Map<String, Long> clickBuffer = new HashMap<>();

    @Autowired
    private UrlRepository urlRepository;

    @KafkaListener(topics = "click-events", groupId = "url-shortener-group")
    public void consume(String shortCode) {

        clickBuffer.put(shortCode,
                clickBuffer.getOrDefault(shortCode, 0L) + 1);

        System.out.println("Buffered click for: " + shortCode);
    }

    // हर 10 sec me DB update
    @Scheduled(fixedRate = 10000)
    public void flushToDB() {

        System.out.println("Flushing to DB: " + clickBuffer);

        clickBuffer.forEach((shortCode, count) -> {
            urlRepository.incrementClickCount(shortCode, count);
        });

        clickBuffer.clear();
    }
}