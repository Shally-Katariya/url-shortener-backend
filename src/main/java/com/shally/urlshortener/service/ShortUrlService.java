package com.shally.urlshortener.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;

@Service
public class ShortUrlService {

    @Autowired
    private UrlRepository urlRepository;

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int LENGTH = 6;

    public String createShortUrl(String longUrl) {
        Optional<Url> existingUrl = urlRepository.findByLongUrl(longUrl);

        if (existingUrl.isPresent()) {
            return existingUrl.get().getShortCode();
        }
        String shortCode;

        do {
            shortCode = generateShortCode();
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        Url url = new Url();

        url.setShortCode(shortCode);
        url.setLongUrl(longUrl);
        url.setCreatedAt(LocalDateTime.now());

        urlRepository.save(url);

        return shortCode;
    }

    private String generateShortCode() {

        Random random = new Random();
        StringBuilder shortCode = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {

            int index = random.nextInt(CHARACTERS.length());
            shortCode.append(CHARACTERS.charAt(index));
        }

        return shortCode.toString();
    }

    public String getLongUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() -> new RuntimeException("URL not found"));
        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);    

             return url.getLongUrl();
}
}