package com.shally.urlshortener.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shally.urlshortener.dto.UrlStatsResponse;
import com.shally.urlshortener.model.UrlRequest;
import com.shally.urlshortener.service.RateLimiterService;
import com.shally.urlshortener.service.UrlService;

@RestController
public class UrlController {

    @Autowired
    private UrlService urlService;

    @Autowired
    private RateLimiterService rateLimiterService;

    // 🔥 PRODUCTION BASE URL (IMPORTANT)
    @Value("${app.base-url}")
    private String baseUrl;

    // ✅ CREATE SHORT URL
    @PostMapping("/api/urls")
    public Map<String, String> createShortUrl(@RequestBody UrlRequest request) {

        String shortCode = urlService.createShortUrl(request.getLongUrl());

        return Map.of(
                "shortUrl", baseUrl + "/" + shortCode
        );
    }

    // ✅ REDIRECT (PUBLIC URL)
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {

        // 🔥 RATE LIMIT CHECK
        if (!rateLimiterService.isAllowed(shortCode)) {
            return ResponseEntity
                    .status(429)
                    .body("Too many requests 🚫");
        }

        String longUrl = urlService.getLongUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    // ✅ GET STATS
    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {

        UrlStatsResponse stats = urlService.getUrlStats(shortCode);

        return ResponseEntity.ok(stats);
    }
}