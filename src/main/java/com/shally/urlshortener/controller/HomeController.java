package com.shally.urlshortener.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shally.urlshortener.dto.UrlStatsResponse;
import com.shally.urlshortener.model.UrlRequest;
import com.shally.urlshortener.service.UrlService;
import com.shally.urlshortener.service.RateLimiterService;
import com.shally.urlshortener.utils.JwtUtil;
import com.shally.urlshortener.model.LoginRequest;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class HomeController {

    @Autowired
    private UrlService shortUrlService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private JwtUtil jwtUtil;

    //@GetMapping("/")
    //public String home() {
      //  return "URL Shortener Backend is running 🚀";
    //}

    @PostMapping("/shorten")
public String shorten(@RequestBody UrlRequest request,
                      HttpServletRequest httpRequest) {

    String authHeader = httpRequest.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new RuntimeException("Missing token");
    }

    String token = authHeader.substring(7);

    String username = jwtUtil.extractUsername(token);

    System.out.println("Authenticated user: " + username);

    String shortCode = shortUrlService.createShortUrl(request.getLongUrl());

    return "Short URL: http://localhost:8080/" + shortCode;
}

    @GetMapping("/{shortCode:[a-zA-Z0-9]{5,15}}")
    public void redirect(@PathVariable String shortCode,
                         HttpServletResponse response,
                         HttpServletRequest request) throws IOException {

        // Rate limiting
        if (!rateLimiterService.isAllowed(shortCode)) {
            response.sendError(429, "Too many requests 🚫"); // ✅ better
            return;
        }

        String longUrl = shortUrlService.getLongUrl(shortCode);
        response.sendRedirect(longUrl);
    }

    @GetMapping("/stats/{shortCode}")
    public UrlStatsResponse getStats(@PathVariable String shortCode) {
        return shortUrlService.getUrlStats(shortCode);
    }

    @PostMapping("/login")
public String login(@RequestBody LoginRequest request) {

    // for now dummy auth
    if ("admin".equals(request.getUsername()) &&
        "password".equals(request.getPassword())) {

        return jwtUtil.generateToken(request.getUsername());
    }

    throw new RuntimeException("Invalid credentials");
}
}