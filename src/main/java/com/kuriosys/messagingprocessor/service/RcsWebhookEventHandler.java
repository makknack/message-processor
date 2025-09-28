package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.kuriosys.messagingprocessor.exception.SkipRecordException;

public interface RcsWebhookEventHandler {
    void handle(JsonNode jsonNode) throws JsonProcessingException, SkipRecordException;
}
