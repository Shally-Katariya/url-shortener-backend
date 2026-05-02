package com.shally.urlshortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final String TOPIC = "url-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendClickEvent(String shortCode) {
        kafkaTemplate.send(TOPIC, shortCode);
    }
}