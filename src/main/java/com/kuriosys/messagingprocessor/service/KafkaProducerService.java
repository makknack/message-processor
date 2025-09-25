package com.kuriosys.messagingprocessor.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String,String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    public void send(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }

    public void send(String topic, String key, String message, int partition) {
        kafkaTemplate.send(topic, partition, key, message);
    }

    public void send(String topic, String key, String message, int partition, long timestamp) {
        kafkaTemplate.send(topic, partition, timestamp, key, message);
    }

}
