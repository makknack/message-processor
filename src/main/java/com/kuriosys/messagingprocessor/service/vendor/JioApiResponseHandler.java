package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.service.ServiceProviderApiResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JioApiResponseHandler implements ServiceProviderApiResponseHandler {

    private final ObjectMapper mapper;

    @Override
    public String handle(String response) throws Exception {
        Map<?, ?> responseMap = mapper.readValue(response, Map.class);
        String referenceId = null;
        if (responseMap.containsKey("referenceID")) {
            referenceId = responseMap.get("referenceID").toString();
        }
        return referenceId;
    }
}
