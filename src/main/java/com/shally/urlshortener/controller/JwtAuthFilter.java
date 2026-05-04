package com.shally.urlshortener.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import com.shally.urlshortener.utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // ✅ PUBLIC ROUTES (NO AUTH REQUIRED)
        if (
            path.equals("/") ||
            path.equals("/index.html") ||
            path.startsWith("/api/urls") ||   // shorten + stats
            path.startsWith("/api/auth") ||   // login
            !path.startsWith("/api")          // 🔥 THIS FIXES REDIRECT
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔒 PROTECTED ROUTES (JWT REQUIRED)
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        try {
            jwtUtil.extractUsername(token); // validate token
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}