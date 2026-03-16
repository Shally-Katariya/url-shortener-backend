package com.shally.urlshortener.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;
import com.shally.urlshortener.utils.Base62Encoder;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public String createShortUrl(String longUrl) {

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setCreatedAt(LocalDateTime.now());

        // Save first to generate ID
        Url savedUrl = urlRepository.save(url);

        // Convert ID → Base62
        String shortCode = Base62Encoder.encode(savedUrl.getId());

        savedUrl.setShortCode(shortCode);

        urlRepository.save(savedUrl);

        return shortCode;
    }

    public String getLongUrl(String shortCode) {

        // 1️⃣ Check Redis cache
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);

        if (cachedUrl != null) {
            return cachedUrl;
        }

        // 2️⃣ Cache miss → query database
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        String longUrl = url.getLongUrl();

        // 3️⃣ Store in Redis
        redisTemplate.opsForValue().set(shortCode, longUrl, Duration.ofHours(1));

        // 4️⃣ Update click analytics
        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return longUrl;
    }
}
