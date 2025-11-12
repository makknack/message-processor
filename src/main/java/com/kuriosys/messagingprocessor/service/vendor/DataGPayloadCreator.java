package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.enums.RcsMessageRecipientStatus;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.RcsExternalTemplate;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import com.kuriosys.messagingprocessor.repository.RcsExternalTemplateRepository;
import com.kuriosys.messagingprocessor.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataGPayloadCreator  implements PayloadCreator {

    @Override
    public Map<String, Object> createPayload(PayloadRequest request) throws ProcessingException {

        Map<String, Object> payload = new HashMap<>();
        payload.put("version", "1.0");
        payload.put("authkey", request.getServiceRouteDetails().getApiKey());
        payload.put("encrpt", "0");
        payload.put("template_id", request.getExternalTemplateId());
        payload.put("country_code", 91);
        payload.put("is_unicode", 0);
        payload.put("sender", request.getRcsExternalAgentId());
        Map<String, Object> message = new HashMap<>();
        message.put("dest", request.getRecipients());
        if(request.isPersonalized()){
            message.put("param", request.getPersonalizedValues());
        }
        else{
            message.put("param",  new HashMap<>());
        }
        payload.put("messages", List.of(message));
        return payload;
    }

    public Map<String, String> getHeaders(PayloadRequest payloadRequest) {
        return Map.of(
                "Content-Type", "application/json");
    }
}
