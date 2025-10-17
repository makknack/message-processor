package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.RcsExternalAgent;
import com.kuriosys.messagingprocessor.model.RcsMessageRequest;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import com.kuriosys.messagingprocessor.service.PayloadCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGPayloadCreator implements PayloadCreator {

    private final RcsMessageRequest rcsMessageRequest;
    private final RcsExternalAgent rcsExternalAgent;
    private final ServiceRouteDetails serviceRouteDetails;
    private final Map<String, String> headers;

    public DataGPayloadCreator(RcsMessageRequest rcsMessageRequest,
                             ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent) {
        this.rcsMessageRequest = rcsMessageRequest;
        this.rcsExternalAgent = rcsExternalAgent;
        this.serviceRouteDetails = serviceRouteDetails;
        this.headers = getHeaders();
    }

    @Override
    public Map<String, Object> createPayload(Object content, List<String> recipients) throws ProcessingException, JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("version", "1.0");
        payload.put("authkey", serviceRouteDetails.getApiKey());
        payload.put("encrpt", "0");
        payload.put("template_id", rcsMessageRequest.getMessageTemplateId());
        payload.put("country_code", "91");
        payload.put("is_unicode", 0);
        payload.put("sender", rcsExternalAgent.getExternalAgentId());
        Map<String, Object> message = new HashMap<>();
        message.put("dest", recipients);
        message.put("param", content != null ? content : new HashMap<>());
        payload.put("messages", List.of(message));
        return payload;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return headers;
    }
}
