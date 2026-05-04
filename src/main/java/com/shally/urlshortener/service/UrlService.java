package com.shally.urlshortener.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.dto.UrlStatsResponse;
import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;
import com.shally.urlshortener.utils.Base62Encoder;
import com.shally.urlshortener.utils.SnowflakeGenerator;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private SnowflakeGenerator snowflakeGenerator;

    // ✅ CREATE SHORT URL
    public String createShortUrl(String longUrl) {

        if (!isValidUrl(longUrl)) {
            throw new IllegalArgumentException("Invalid or unsafe URL");
        }

        Optional<Url> existing = urlRepository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        long id = snowflakeGenerator.nextId();
        String shortCode = Base62Encoder.encode(id);

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);

        urlRepository.save(url);

        // Kafka event (optional)
        try {
            if (kafkaProducerService != null) {
                kafkaProducerService.sendClickEvent("URL_CREATED: " + shortCode);
            }
        } catch (Exception ignored) {}

        return shortCode;
    }

    // ✅ GET LONG URL (OPTIMIZED)
    public String getLongUrl(String shortCode) {

        String cachedUrl = null;

        // 1️⃣ Check Redis
        try {
            cachedUrl = redisTemplate.opsForValue().get(shortCode);
        } catch (Exception e) {
            System.out.println("Redis GET failed");
        }

        // ✅ CACHE HIT (NO DB CALL AT ALL)
        if (cachedUrl != null) {
            System.out.println("🔥 CACHE HIT");

            // Only send Kafka event (async analytics)
            try {
                if (kafkaProducerService != null) {
                    kafkaProducerService.sendClickEvent(shortCode);
                }
            } catch (Exception ignored) {}

            return cachedUrl; // 🚀 immediate return
        }

        // ❌ CACHE MISS → DB CALL
        System.out.println("❌ CACHE MISS");

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        String longUrl = url.getLongUrl();

        // 2️⃣ Store in Redis
        try {
            redisTemplate.opsForValue().set(shortCode, longUrl, Duration.ofHours(1));
        } catch (Exception e) {
            System.out.println("Redis SET failed");
        }

        // 3️⃣ Send Kafka event
        try {
            if (kafkaProducerService != null) {
                kafkaProducerService.sendClickEvent(shortCode);
            }
        } catch (Exception ignored) {}

        return longUrl;
    }

    // ✅ STATS
    public UrlStatsResponse getUrlStats(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        return new UrlStatsResponse(
                url.getShortCode(),
                url.getLongUrl(),
                url.getClickCount(),
                url.getCreatedAt()
        );
    }

    // ✅ URL VALIDATION
    private boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null &&
                   (uri.getScheme().equalsIgnoreCase("http") ||
                    uri.getScheme().equalsIgnoreCase("https"));
        } catch (Exception e) {
            return false;
        }
    }
}