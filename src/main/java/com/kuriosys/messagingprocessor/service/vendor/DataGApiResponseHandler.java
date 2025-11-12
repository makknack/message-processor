package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.service.PayloadRequest;
import com.kuriosys.messagingprocessor.service.ServiceProviderApiResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGApiResponseHandler implements ServiceProviderApiResponseHandler {

    private final ObjectMapper objectMapper;

    @Override
    public String handle(String responseBody)  {
        String referenceId = null;
        if (responseBody == null || responseBody.isEmpty()) return null;
        java.util.Map<?,?> responseMap = null;
        try {
            responseMap = objectMapper.readValue(responseBody, java.util.Map.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing response body: {}", e.getMessage());
            return null;
        }
        if (responseMap.containsKey("LogID")) {
            referenceId = responseMap.get("LogID").toString();
        }

        log.debug("Parsed referenceId: {}", referenceId);
        // Do not persist here; persistence is handled by RecipientPersistenceService
        return referenceId;
    }
}
