package com.shally.urlshortener.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shally.urlshortener.model.UrlRequest;
import com.shally.urlshortener.service.ShortUrlService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class HomeController {

    @Autowired
    private ShortUrlService shortUrlService;

    @GetMapping("/")
    public String home() {
        return "URL Shortener Backend is running 🚀";
    }

    @PostMapping("/shorten")
    public String shorten(@RequestBody UrlRequest request) {

        String shortCode = shortUrlService.createShortUrl(request.getUrl());

        return "Short URL: http://localhost:8080/" + shortCode;
    }
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletResponse response) throws IOException {

        String longUrl = shortUrlService.getLongUrl(shortCode);

        response.sendRedirect(longUrl);
}
}