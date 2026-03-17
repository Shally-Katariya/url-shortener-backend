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

   public String createShortUrl(String longUrl) {

    Optional<Url> existing = urlRepository.findByLongUrl(longUrl);

    if (existing.isPresent()) {
        return existing.get().getShortCode();
    }

    Url url = new Url();
    url.setLongUrl(longUrl);
    url.setCreatedAt(LocalDateTime.now());

    Url savedUrl = urlRepository.save(url);

    String shortCode = Base62Encoder.encode(savedUrl.getId());

    savedUrl.setShortCode(shortCode);

    urlRepository.save(savedUrl);

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
}
