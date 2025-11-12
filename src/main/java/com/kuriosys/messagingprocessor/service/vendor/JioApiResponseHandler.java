package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.service.ServiceProviderApiResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JioApiResponseHandler implements ServiceProviderApiResponseHandler {

    private final ObjectMapper mapper;

    @Override
    public String handle(String response) {
        Map<?, ?> responseMap = null;
        try {
            responseMap = mapper.readValue(response, Map.class);
        } catch (JsonProcessingException e) {
           log.warn("Failed to parse Jio response: {}", e.getMessage());
        }
        String referenceId = null;
        if (responseMap!=null && responseMap.containsKey("referenceID")) {
            referenceId = responseMap.get("referenceID").toString();
        }
        return referenceId;
    }
}
