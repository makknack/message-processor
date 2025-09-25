package com.kuriosys.messagingprocessor.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.enums.EventType;
import com.kuriosys.messagingprocessor.event.RcsSubmissionEvent;
import com.kuriosys.messagingprocessor.service.RcsSubmissionEventHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessagingEventListeners {

    private static final Logger log = LoggerFactory.getLogger(MessagingEventListeners.class);

    private final RcsSubmissionEventHandler rcsSubmissionEventHandler;
    private final ObjectMapper objectMapper;


    @KafkaListener(id="${kafka.consumers.rcs-response.id}",
            topics = "${kafka.topics.rcs-response}",
            groupId = "${kafka.consumers.rcs-response.group-id}",
            autoStartup = "${kafka.consumers.rcs-response.enabled:false}",
            containerFactory = "kafkaListenerContainerFactory")
    public void rcsWebHookEvents(List<ConsumerRecord<String,String>> records, Acknowledgment acknowledgment){
        log.debug("RCS response event received, number of records: {}", records.size());
        for(ConsumerRecord<String,String> record : records){
            log.debug("RCS response event received: {}", record.value());
            try {
                JsonNode root = objectMapper.readTree(record.value());
                if (root.has("reachableUsers")) {
                    JsonNode reachableUsersNode = root.get("reachableUsers");
                    if (reachableUsersNode != null && reachableUsersNode.isArray()) {
                        for (JsonNode userNode : reachableUsersNode) {
                            String user = userNode.asText();
                            log.info("Reachable user: {}", user);
                            // TODO: Add your business logic here (e.g., update DB, metrics, etc.)
                        }
                    }
                }
                // TODO: Add parsing/handling for other event types as needed
            } catch (Exception e) {
                log.error("Error parsing RCS webhook event: {}", record.value(), e);
            }
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(id="${kafka.consumers.rcs-submission.id}",
            topics = "${kafka.topics.rcs-submission}",
            groupId = "${kafka.consumers.rcs-submission.group-id}",
            autoStartup = "${kafka.consumers.rcs-submission.enabled:false}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleRcsSubmissionEvents(List<ConsumerRecord<String, String>> records,
                                          Acknowledgment acknowledgment) {
        try {
            for(ConsumerRecord<String, String> record : records) {
                log.info("Processing RCS File Upload Event: {}", record.offset());
                JsonNode root = objectMapper.readTree(record.value());
                String eventTypeStr = root.get("eventType").asText();
                EventType eventType = EventType.fromValue(eventTypeStr);
                RcsSubmissionEvent rcsSubmissionEvent;
                switch (eventType) {
                    case RCS_FILE_MSG_REQ:
                        rcsSubmissionEvent = objectMapper.readValue(record.value(), RcsSubmissionEvent.class);
                        rcsSubmissionEventHandler.sendToFileNumbers(rcsSubmissionEvent);
                        break;
                    case RCS_GROUP_MSG_REQ:
                        rcsSubmissionEvent = objectMapper.readValue(record.value(), RcsSubmissionEvent.class);
                        rcsSubmissionEventHandler.sendToGroupNumbers(rcsSubmissionEvent);
                        break;
                    case RCS_NUMBER_MSG_REQ:
                        rcsSubmissionEvent = objectMapper.readValue(record.value(), RcsSubmissionEvent.class);
                        rcsSubmissionEventHandler.sendToManualNumbers(rcsSubmissionEvent);
                        break;
                    case null:
                        log.warn("Event type is null in the event: {}", record.value());
                        break;
                    default:
                        log.warn("Unknown event type: {}", eventType);
                        return;
                }
                acknowledgment.acknowledge();
                log.debug("RCS File Upload Event processed: {} {}", eventType,  record.offset());
            }
        }
        catch (Exception e){
            log.error("Exception while processing rcs submission events ", e);
            // TODO : Decide for which exceptions it should be rolled back  and for which it should not
            acknowledgment.acknowledge();
        }

    }
}
