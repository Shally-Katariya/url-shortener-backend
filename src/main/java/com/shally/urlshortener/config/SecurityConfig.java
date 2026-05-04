package com.shally.urlshortener.config;  // fix package too

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.shally.urlshortener.controller.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

   // @Autowired
    //private JwtAuthFilter jwtAuthFilter;

   @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", 
                "/index.html",
                "/api/urls/**",
                "/**"   // 🔥 THIS LINE FIXES REDIRECT
            ).permitAll()
            .anyRequest().permitAll()
        );

    return http.build();
}
}