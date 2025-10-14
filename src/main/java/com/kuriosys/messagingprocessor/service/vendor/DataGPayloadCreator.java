package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.RcsExternalAgent;
import com.kuriosys.messagingprocessor.model.RcsMessageRequest;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import com.kuriosys.messagingprocessor.service.PayloadCreator;

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
        return Map.of();
    }

    @Override
    public Map<String, String> getHeaders() {
        return Map.of();
    }
}
