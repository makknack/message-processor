package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kuriosys.messagingprocessor.exception.ProcessingException;

import java.util.List;
import java.util.Map;

public interface PayloadCreator {

    Map<String, Object> createPayload(PayloadRequest payloadRequest) throws ProcessingException, JsonProcessingException;
    Map<String, String> getHeaders(PayloadRequest payloadRequest);
}
