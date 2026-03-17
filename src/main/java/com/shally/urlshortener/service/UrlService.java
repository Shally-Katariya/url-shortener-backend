package com.shally.urlshortener.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.dto.UrlStatsResponse;
import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;
import com.shally.urlshortener.utils.Base62Encoder;
import com.shally.urlshortener.utils.SnowflakeGenerator;
import java.net.URI;


@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private SnowflakeGenerator snowflakeGenerator;

  public String createShortUrl(String longUrl) {

    // 🔒 VALIDATION (ADD THIS)
    if (!isValidUrl(longUrl)) {
        throw new IllegalArgumentException("Invalid or unsafe URL");
    }

    Optional<Url> existing = urlRepository.findByLongUrl(longUrl);

    if (existing.isPresent()) {
        return existing.get().getShortCode();
    }

    // 🔥 generate ID using Snowflake
    long id = snowflakeGenerator.nextId();

    String shortCode = Base62Encoder.encode(id);

    Url url = new Url();
    url.setLongUrl(longUrl);
    url.setShortCode(shortCode);
    url.setCreatedAt(LocalDateTime.now());
    url.setClickCount(0L);

    urlRepository.save(url);

    return shortCode;
}

   

   public String getLongUrl(String shortCode) {

    // 1️⃣ Check Redis cache
    String cachedUrl = redisTemplate.opsForValue().get(shortCode);

    if (cachedUrl != null) {

        
       kafkaProducerService.sendClickEvent(shortCode);

        return cachedUrl;
    }

    // 2️⃣ Cache miss → query DB
    Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new RuntimeException("URL not found"));

    String longUrl = url.getLongUrl();

    // store URL in Redis cache
    redisTemplate.opsForValue().set(shortCode, longUrl, Duration.ofHours(1));

    
    kafkaProducerService.sendClickEvent(shortCode);

    return longUrl;
}

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
