package com.shally.urlshortener.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;

@Service
public class AnalyticsWorker {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlRepository urlRepository;

    @Scheduled(fixedRate = 60000) // every 10 seconds
    public void flushClickCounts() {

        Set<String> keys = redisTemplate.keys("clicks:*");

        if (keys == null) return;

        for (String key : keys) {

            String shortCode = key.replace("clicks:", "");

            String value = redisTemplate.opsForValue().get(key);

            if (value == null) continue;

            long count = Long.parseLong(value);

            Url url = urlRepository.findByShortCode(shortCode).orElse(null);

            if (url != null) {

                url.setClickCount(url.getClickCount() + count);
                urlRepository.save(url);
            }

            redisTemplate.delete(key);
        }
    }
}