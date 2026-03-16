package com.shally.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.shally.urlshortener.model.Url;
import com.shally.urlshortener.repository.UrlRepository;

@Service
public class AnalyticsService {

    @Autowired
    private UrlRepository urlRepository;

    @Async
    public void updateClickCount(Url url) {

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

    }
}