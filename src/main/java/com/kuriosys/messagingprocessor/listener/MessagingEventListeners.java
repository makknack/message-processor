package com.kuriosys.messagingprocessor.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.enums.EventType;
import com.kuriosys.messagingprocessor.enums.RcsEventLogStatus;
import com.kuriosys.messagingprocessor.enums.RcsWebhookEventSource;
import com.kuriosys.messagingprocessor.event.RcsSubmissionEvent;
import com.kuriosys.messagingprocessor.exception.SkipRecordException;
import com.kuriosys.messagingprocessor.model.RcsEventLog;
import com.kuriosys.messagingprocessor.model.jio.MessageRequestSendEvent;
import com.kuriosys.messagingprocessor.repository.RcsEventLogRepository;
import com.kuriosys.messagingprocessor.service.RcsSubmissionEventHandler;
import com.kuriosys.messagingprocessor.service.RcsWebhookEventHandler;
import com.kuriosys.messagingprocessor.service.vendor.JioRcsWebhookEventHandler;
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
    private final JioRcsWebhookEventHandler jioRcsWebhookEventHandler;
    private final RcsEventLogRepository rcsEventLogRepository;


    @KafkaListener(id="${kafka.consumers.rcs-response.id}",
            topics = "${kafka.topics.rcs-response}",
            groupId = "${kafka.consumers.rcs-response.group-id}",
            autoStartup = "${kafka.consumers.rcs-response.enabled:false}",
            containerFactory = "kafkaListenerContainerFactory")
    public void rcsWebHookEvents(List<ConsumerRecord<String,String>> records, Acknowledgment acknowledgment){
        log.debug("RCS response event received, number of records: {}", records.size());
        RcsWebhookEventHandler rcsWebhookEventHandler = null;
        for(ConsumerRecord<String,String> record : records) {
            log.info("Processing RCS Webhook Event: {}", record.offset());
            JsonNode jsonNode = null;
            String eventId = null;
            String source = null;
            String eventType = null;
            try {
                jsonNode = objectMapper.readTree(record.value());
                source = jsonNode.get("source").asText();
                eventId = jsonNode.get("eventId").asText();
                eventType = jsonNode.get("eventType").asText();
                RcsWebhookEventSource rcsResponseEventSource =  RcsWebhookEventSource.valueOf(source);
                if(RcsWebhookEventSource.JIO == rcsResponseEventSource){
                    rcsWebhookEventHandler = jioRcsWebhookEventHandler;
                }
                else {
                    log.warn("No Implementation for this source {}", source);
                }
                rcsWebhookEventHandler.handle(jsonNode);
            }
            catch(JsonProcessingException e){
                log.warn("Exception while processing rcs response events Exception={}, Topic={}, Partition={}, Offset={}, Event={}", e.getMessage(), record.topic(),record.partition(),record.offset(),record.value());
            }
            catch(SkipRecordException e){
                logRcsEvent(eventId, source, eventType, record.value(), RcsEventLogStatus.NO_ACTION_NEEDED,"Skipping record:");
            }
            catch (Exception e) {
                log.error("Exception while processing rcs response events " +record.value(), e);
                logRcsEvent(eventId, source, eventType, record.value(), RcsEventLogStatus.NEED_PROCESSING,e.getMessage());
            }
        }
        acknowledgment.acknowledge();
    }

    private void logRcsEvent(String eventId, String source, String eventType, String payload, RcsEventLogStatus rcsEventLogStatus, String comment) {
        RcsEventLog rcsEventLog = new RcsEventLog();
        rcsEventLog.setSource(source);
        rcsEventLog.setEventId(eventId);
        rcsEventLog.setPayload(payload);
        rcsEventLog.setEventType(eventType);
        rcsEventLog.setStatus(rcsEventLogStatus);
        rcsEventLog.setComment(comment);
        rcsEventLogRepository.save(rcsEventLog);
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
