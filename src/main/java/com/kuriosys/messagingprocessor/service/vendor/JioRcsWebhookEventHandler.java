package com.kuriosys.messagingprocessor.service.vendor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.exception.SkipRecordException;
import com.kuriosys.messagingprocessor.model.RcsResponseEvent;
import com.kuriosys.messagingprocessor.model.jio.Error;
import com.kuriosys.messagingprocessor.model.jio.JioWebhookEvent;
import com.kuriosys.messagingprocessor.repository.RcsEventLogRepository;
import com.kuriosys.messagingprocessor.repository.RcsResponseEventRepository;
import com.kuriosys.messagingprocessor.service.RcsWebhookEventHandler;
import com.kuriosys.messagingprocessor.util.EventTypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


@Slf4j
@RequiredArgsConstructor
@Service
public class JioRcsWebhookEventHandler implements RcsWebhookEventHandler {

    private final ObjectMapper objectMapper;
    private final RcsResponseEventRepository rcsResponseEventRepository;
    private final RcsEventLogRepository rcsEventLogRepository;

    @Override
    public void handle(JsonNode jsonNode) throws JsonProcessingException, SkipRecordException {
        log.debug("Handling Jio RCS response event: {}", jsonNode);

        JsonNode payloadNode=  jsonNode.get("payload");
        String payloadAsText = jsonNode.get("payload").asText();
        String eventId = jsonNode.get("eventId").asText();
        String source = jsonNode.get("source").asText();
        String eventType = jsonNode.get("eventType").asText();
        if(!payloadNode.isEmpty()) {
            JsonNode entityNode =  jsonNode.get("payload").get("entity");
            if(entityNode!=null && !entityNode.isEmpty()) {
                    JioWebhookEvent webhookEvent = objectMapper.treeToValue(payloadNode, JioWebhookEvent.class);
                    String recipient =  stripLeadingPlus(webhookEvent.getUserPhoneNumber());
                    String externalEventType = webhookEvent.getEntity().getEventType();
                    String agentId = webhookEvent.getAgentId();
                    String referenceId= webhookEvent.getEntity().getReferenceID();
                    String messageRequestId= webhookEvent.getEntity().getMessageId();
                    OffsetDateTime sendTime = webhookEvent.getEntity().getSendTime();
                    Error error = webhookEvent.getEntity().getError();
                    RcsResponseEvent rcsResponseEvent  = new RcsResponseEvent();
                    rcsResponseEvent.setAgentId(agentId);
                    rcsResponseEvent.setUserPhoneNumber(recipient);
                    rcsResponseEvent.setReferenceId(referenceId);
                    rcsResponseEvent.setMessageRequestId(messageRequestId);
                    rcsResponseEvent.setEventId(eventId);
                    rcsResponseEvent.setSourceEventTime(sendTime);
                    rcsResponseEvent.setEventType(EventTypeConverter.getEvent(externalEventType).name());
                    rcsResponseEvent.setPayload(payloadNode);
                    if(error!=null){
                        rcsResponseEvent.setErrorDetails(objectMapper.writeValueAsString(error));
                    }
                    log.debug("Parsed RcsResponseEvent: {}", rcsResponseEvent);
                    rcsResponseEventRepository.save(rcsResponseEvent);
            }
            else {
                throw new SkipRecordException();
            }
        }
        else{
            log.warn("Payload is empty in the received JSON {}", jsonNode);
        }
    }

    private String stripLeadingPlus(String phoneNumber){
        if(phoneNumber!=null && phoneNumber.startsWith("+")){
            return phoneNumber.substring(1);
        }
        return phoneNumber;
    }

}
