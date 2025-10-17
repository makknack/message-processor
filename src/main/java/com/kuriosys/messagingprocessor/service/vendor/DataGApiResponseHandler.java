package com.kuriosys.messagingprocessor.service.vendor;

import com.kuriosys.messagingprocessor.service.ServiceProviderApiResponseHandler;

public class DataGApiResponseHandler implements ServiceProviderApiResponseHandler {
    @Override
    public String handle(String response) throws Exception {
        // Parse the response and return LogID as referenceId
        if (response == null || response.isEmpty()) return null;
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<?,?> responseMap = mapper.readValue(response, java.util.Map.class);
        if (responseMap.containsKey("LogID")) {
            return responseMap.get("LogID").toString();
        }
        return null;
    }
}
