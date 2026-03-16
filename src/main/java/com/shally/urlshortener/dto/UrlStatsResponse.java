package com.shally.urlshortener.dto;

import java.time.LocalDateTime;

public class UrlStatsResponse {

    private String shortCode;
    private String longUrl;
    private long clickCount;
    private LocalDateTime createdAt;

    public UrlStatsResponse(String shortCode, String longUrl, long clickCount, LocalDateTime createdAt) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public long getClickCount() {
        return clickCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}