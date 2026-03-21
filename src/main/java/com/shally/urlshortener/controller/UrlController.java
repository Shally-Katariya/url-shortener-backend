
package com.shally.urlshortener.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shally.urlshortener.dto.UrlStatsResponse;
import com.shally.urlshortener.model.UrlRequest;
import com.shally.urlshortener.service.RateLimiterService;
import com.shally.urlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @Autowired
private RateLimiterService rateLimiterService;

 @PostMapping("/urls")
public Map<String, String> createShortUrl(@RequestBody UrlRequest request,
                                          HttpServletRequest httpRequest) {

    String shortCode = urlService.createShortUrl(request.getLongUrl());

    String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName();

    return Map.of(
            "shortUrl", baseUrl + "/" + shortCode
    );
}

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

    @GetMapping("/urls/{shortCode}/stats")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {

    UrlStatsResponse stats = urlService.getUrlStats(shortCode);

    return ResponseEntity.ok(stats);
}
@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
}
