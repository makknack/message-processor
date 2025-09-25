package com.kuriosys.messagingprocessor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.event.RcsSubmissionEvent;
import com.kuriosys.messagingprocessor.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaProducerController {

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    public KafkaProducerController(KafkaProducerService kafkaProducerService, ObjectMapper objectMapper) {
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = objectMapper;
    }

    @Value("${kafka.topics.rcs-response")
    private String rcsWebhookEventsTopic;

    @Value("${kafka.topics.rcs-submission}")
    private String rcsFileUploadEventTopic;

    @PostMapping("/sendRcsWebhookEvent")
    public String sendMessage(String message) {
        kafkaProducerService.send(rcsWebhookEventsTopic, message);
        return "Message sent to topic " + rcsWebhookEventsTopic;
    }

    @PostMapping("/sendRcsFileSubmissionEvent")
    public String sendRcsFileUploadEvent(@RequestBody RcsSubmissionEvent rcsFileSubmissionEvent) throws JsonProcessingException {
        kafkaProducerService.send(rcsFileUploadEventTopic, objectMapper.writeValueAsString(rcsFileSubmissionEvent));
        return "RCS file upload event sent to topic " + rcsFileUploadEventTopic;
    }

}
