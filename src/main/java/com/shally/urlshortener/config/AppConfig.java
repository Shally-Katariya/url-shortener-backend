package com.shally.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shally.urlshortener.utils.SnowflakeGenerator;

@Configuration
public class AppConfig {

    @Bean
    public SnowflakeGenerator snowflakeGenerator() {
        return new SnowflakeGenerator(1);
    }
}