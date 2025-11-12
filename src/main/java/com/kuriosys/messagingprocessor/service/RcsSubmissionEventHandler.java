package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.constant.Constants;
import com.kuriosys.messagingprocessor.enums.*;
import com.kuriosys.messagingprocessor.model.*;
import com.kuriosys.messagingprocessor.repository.*;
import com.kuriosys.messagingprocessor.event.RcsSubmissionEvent;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.service.vendor.DataGApiResponseHandler;
import com.kuriosys.messagingprocessor.service.vendor.DataGPayloadCreator;
import com.kuriosys.messagingprocessor.service.vendor.JioApiResponseHandler;
import com.kuriosys.messagingprocessor.service.vendor.JioPayloadCreator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.kuriosys.messagingprocessor.enums.EventType.*;

@Service
@RequiredArgsConstructor
public class RcsSubmissionEventHandler {

    private final Logger log = LoggerFactory.getLogger(RcsSubmissionEventHandler.class);

    private final RcsMessageRequestRepository rcsMessageRequestRepository;
    private final RcsMessageRecipientRepository rcsMessageRecipientRepository;
    private final ObjectMapper objectMapper;
    private final RcsMessageTemplateRepository rcsMessageTemplateRepository;
    private final ContactRepository contactRepository;
    private final ServiceRouteDetailsRepository serviceRouteDetailsRepository;
    private final RcsExternalAgentRepository rcsExternalAgentRepository;
    private final JioApiResponseHandler jioApiResponseHandler;
    private final DataGApiResponseHandler dataGApiResponseHandler;
    private final WebClient webClient;
    private final static int DB_FETCH_SIZE = 200; // Batch size for processing contacts
    private final RcsExternalTemplateRepository rcsExternalTemplateRepository;
    private final JioPayloadCreator jioPayloadCreator;
    private final DataGPayloadCreator dataGPayloadCreator;
    private final MessageSendingService messageSendingService;
    private final RcsMessageRequestService rcsMessageRequestService;
    private final RcsMessageRecipientService rcsMessageRecipientService;

    private final Map<ServiceProvider, ServiceProviderApiResponseHandler> serviceProviderApiResponseHandlerMap = new HashMap<>();
    private final Map<ServiceProvider, PayloadCreator> payloadCreatorMap = new HashMap<>();

    @PostConstruct
    public void registerServiceProviderHandlers() {
        serviceProviderApiResponseHandlerMap.put(ServiceProvider.JIO, jioApiResponseHandler);
        serviceProviderApiResponseHandlerMap.put(ServiceProvider.DATAG, dataGApiResponseHandler);
        payloadCreatorMap.put(ServiceProvider.JIO, jioPayloadCreator);
        payloadCreatorMap.put(ServiceProvider.DATAG, dataGPayloadCreator);
    }

    public void handle(RcsSubmissionEvent rcsSubmissionEvent) throws IOException, ProcessingException {
        RcsMessageRequest rcsMessageRequest = null;
        EventType eventType = EventType.fromValue(rcsSubmissionEvent.getEventType());
        try {
            rcsMessageRequest = rcsMessageRequestRepository.findByMessageRequestIdAndStatusIn(rcsSubmissionEvent.getMessageRequestId(), List.of(RcsMessageRequestStatus.PENDING))
                    .orElseThrow(() -> new ProcessingException("RCS Message Request not found for ID: " + rcsSubmissionEvent.getMessageRequestId()));
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.PROCESSING);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequestRepository.save(rcsMessageRequest);

            List<String> recipients = rcsSubmissionEvent.getRecipients();

            if (recipients == null || recipients.isEmpty()) {
                throw new ProcessingException("No recipients provided for manual submission");
            }

            RcsMessageTemplate rcsMessageTemplate = rcsMessageTemplateRepository.findById(rcsMessageRequest.getMessageTemplateId())
                    .orElseThrow(() -> new ProcessingException("Message template not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));

            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();
            ServiceProvider serviceProvider = ServiceProvider.valueOf(serviceRouteDetails.getServiceProvider());

            RcsExternalTemplate rcsExternalTemplate = null;
            PayloadCreator payloadCreator = null;
            ServiceProviderApiResponseHandler responseHandler = null;
            if (ServiceProvider.JIO.name().equalsIgnoreCase(serviceRouteDetails.getServiceProvider())) {

            } else if (ServiceProvider.DATAG.name().equalsIgnoreCase(serviceRouteDetails.getServiceProvider())) {
                rcsExternalTemplate = rcsExternalTemplateRepository.findByServiceRouteIdAndMessageTemplateId( serviceRouteDetails.getServiceRouteId(), rcsMessageTemplate.getMessageTemplateId()).orElseThrow(
                        () -> new ProcessingException("External template not found for id: " + rcsMessageTemplate.getMessageTemplateId())
                );
            } else {
                throw new ProcessingException("Invalid service provider " + serviceRouteDetails.getServiceProvider());
            }

            if (RCS_NUMBER_MSG_REQ == eventType) {
                sendToManualNumbers(rcsSubmissionEvent, rcsMessageRequest, rcsMessageTemplate, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            } else if (RCS_FILE_MSG_REQ == eventType) {
                processFile(rcsSubmissionEvent, rcsMessageRequest, rcsMessageTemplate, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            } else if (RCS_GROUP_MSG_REQ == eventType) {
                sendMessagesToGroup(rcsSubmissionEvent, rcsMessageRequest, rcsMessageTemplate, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            }


        } catch (ProcessingException | IOException e) {
            log.error("Processing exception occurred: {}", rcsSubmissionEvent.getMessageRequestId(), e);
            if (rcsMessageRequest != null) {
                rcsMessageRequestService.updateMessageRequestStatus(rcsMessageRequest, RcsMessageRequestStatus.FAILED, e.getMessage());
            }
            throw e;
        }
    }

    private void processFile(RcsSubmissionEvent rcsSubmissionEvent, RcsMessageRequest rcsMessageRequest, RcsMessageTemplate rcsMessageTemplate, ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent, RcsExternalTemplate rcsExternalTemplate) throws IOException, ProcessingException {
        CsvReaderService csvReaderService = null;

        if (rcsMessageRequest.getIsPersonalized()) {
            csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, 1);
            csvReaderService.skipRecords(rcsMessageRequest.getProcessedRecord());

            while (csvReaderService.hasMoreRecords()) {
                Map<String, String> nextRecordMap = csvReaderService.getNextRecordAsMap();
                String recipient = nextRecordMap.get(Constants.FILE_HEADER_RECIPIENTS);
                processMessages(rcsMessageRequest, parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType()), nextRecordMap, List.of(recipient), serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            }

        } else {

            int requestBatchSize = serviceRouteDetails.getRequestBatchSize();
            csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, requestBatchSize);
            csvReaderService.skipRecords(rcsMessageRequest.getProcessedRecord());

            while (csvReaderService.hasMoreRecords()) {
                List<String> recipients = csvReaderService.getNextBatchOfFirstColumn(requestBatchSize);
                if (recipients == null || recipients.isEmpty()) {
                    break;
                }
                processMessages(rcsMessageRequest, parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType()), null, recipients, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            }
        }
    }

    private void sendToManualNumbers(RcsSubmissionEvent rcsSubmissionEvent, RcsMessageRequest rcsMessageRequest, RcsMessageTemplate rcsMessageTemplate, ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent, RcsExternalTemplate rcsExternalTemplate) throws JsonProcessingException, ProcessingException {
        List<String> recipients = rcsSubmissionEvent.getRecipients();
        recipients = recipients.subList(rcsMessageRequest.getProcessedRecord(), recipients.size()); // skip processed records

        if (rcsMessageRequest.getIsPersonalized()) {
            Object contentObject = parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType());
            for (int i = 0; i < recipients.size(); i++) {
                Map<String, String> personalizedValues = rcsSubmissionEvent.getTemplateVariables().get(i);
                processMessages(rcsMessageRequest, contentObject, personalizedValues, List.of(recipients.get(i)), serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
            }
        } else {
            Object contentObject = parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType());
            final int requestBatchSize = serviceRouteDetails.getRequestBatchSize();
            int startIndex = 0;
            int total = recipients.size();
            while (startIndex < total) {
                int endIndex = Math.min(startIndex + requestBatchSize, total);
                List<String> recipientsBatch = recipients.subList(startIndex, endIndex);
                processMessages(rcsMessageRequest, contentObject, null, recipientsBatch, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
                startIndex = endIndex;
            }
        }
    }

    private PayloadCreator getPayloadCreator(String serviceProvider) throws ProcessingException {
        ServiceProvider serviceProviderEnum = ServiceProvider.valueOf(serviceProvider);
        PayloadCreator payloadCreator = payloadCreatorMap.get(serviceProviderEnum);
        if (payloadCreator == null) {
            throw new ProcessingException("No response handler found for service provider " + serviceProvider);
        }
        return payloadCreator;
    }

    private ServiceProviderApiResponseHandler getResponseHandler(String serviceProvider) throws ProcessingException {
        ServiceProvider serviceProviderEnum = ServiceProvider.valueOf(serviceProvider);
        ServiceProviderApiResponseHandler responseHandler = serviceProviderApiResponseHandlerMap.get(serviceProviderEnum);
        if (responseHandler == null) {
            throw new ProcessingException("No response handler found for service provider " + serviceProvider);
        }
        return responseHandler;
    }

    private void processMessages(RcsMessageRequest rcsMessageRequest, Object contentObject, Map<String, String> personalizedValues, List<String> recipients, ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent, RcsExternalTemplate rcsExternalTemplate) throws ProcessingException, JsonProcessingException {
        int invalidRecipients = 0;
        int validRecipients = 0;
        RecipientValidationResult recipientValidationResult = validateRecipients(recipients);
        if (!recipientValidationResult.validNumbers().isEmpty()) {
            PayloadRequest.PayloadRequestBuilder payloadRequestBuilder = PayloadRequest.builder();

            Object personalizedContent = null;
            if (rcsMessageRequest.getIsPersonalized()) {
                personalizedContent = formPersonalizedContentObject(contentObject, rcsMessageRequest.getContentType(), personalizedValues);
                payloadRequestBuilder.personalizedContent(personalizedContent);
                payloadRequestBuilder.personalizedValues(personalizedValues);
            } else {
                payloadRequestBuilder.content(contentObject);
            }
            PayloadRequest payloadRequest = payloadRequestBuilder
                    .userId(rcsMessageRequest.getUserId())
                    .messageRequestId(rcsMessageRequest.getMessageRequestId())
                    .contentType(rcsMessageRequest.getContentType())
                    .personalized(rcsMessageRequest.getIsPersonalized())
                    .rcsExternalAgentId(rcsExternalAgent.getAgentId())
                    .externalTemplateId(rcsExternalTemplate != null ? rcsExternalTemplate.getExternalTemplateId() : null)
                    .serviceRouteDetails(serviceRouteDetails)
                    .recipients(recipientValidationResult.validNumbers())
                    .build();
            messageSendingService.sendAndPersistSync(payloadRequest, getPayloadCreator(serviceRouteDetails.getServiceProvider()), getResponseHandler(serviceRouteDetails.getServiceProvider()));
            validRecipients = recipientValidationResult.validNumbers().size();
        }

        if (!recipientValidationResult.invalidNumbers().isEmpty()) {
            rcsMessageRecipientService.insertFailedRecipients(rcsMessageRequest.getUserId(), rcsMessageRequest.getMessageRequestId(), recipientValidationResult.invalidNumbers(), RcsMessageRecipientStatus.SENDING_FAILED);
            invalidRecipients = recipientValidationResult.invalidNumbers().size();
        }
        rcsMessageRequestService.updateMessageRequestSummary(rcsMessageRequest, RcsMessageRequestStatus.PROCESSING, recipients.size(), validRecipients, invalidRecipients);
    }

    private void sendMessagesToGroup(RcsSubmissionEvent rcsSubmissionEvent, RcsMessageRequest rcsMessageRequest, RcsMessageTemplate rcsMessageTemplate, ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent, RcsExternalTemplate rcsExternalTemplate) throws ProcessingException, IOException {
        try {
            long totalContacts = contactRepository.countByGroupId(rcsMessageRequest.getContactGroupId());
            long currentOffset = rcsMessageRequest.getProcessedRecord();
            if (rcsMessageRequest.getIsPersonalized()) {
                CsvReaderService csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, 1);
                long totalFileRecords = csvReaderService.getTotalRecords();

                if (totalContacts != totalFileRecords) {
                    log.warn("Mismatch: contacts in group ({}) != records in file ({}) for messageRequestId {}", totalContacts, totalFileRecords, rcsMessageRequest.getMessageRequestId());
                    throw new ProcessingException("Number of contacts in group does not match number of records in file");
                }
                List<String> recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, DB_FETCH_SIZE);
                while (recipients != null && !recipients.isEmpty()) {
                    for (String recipient : recipients) {
                        Map<String, String> nextRecordMap = csvReaderService.getNextRecordAsMap();
                        processMessages(rcsMessageRequest, parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType()), nextRecordMap, List.of(recipient), serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
                    }
                    recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, DB_FETCH_SIZE);
                }
            } else {
                int requestBatchSize = serviceRouteDetails.getRequestBatchSize();
                List<String> recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, DB_FETCH_SIZE);
                while (recipients != null && !recipients.isEmpty()) {
                    int startIndex = 0;
                    int total = recipients.size();
                    while (startIndex < total) {
                        int endIndex = Math.min(startIndex + requestBatchSize, total);
                        List<String> recipientBatch = recipients.subList(startIndex, endIndex);
                        processMessages(rcsMessageRequest, parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType()), null, recipientBatch, serviceRouteDetails, rcsExternalAgent, rcsExternalTemplate);
                        startIndex = endIndex;
                    }
                    currentOffset = rcsMessageRequest.getProcessedRecord();
                    recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, DB_FETCH_SIZE);
                }

            }
        } catch (ProcessingException e) {
            log.error("Processing exception occurred", e);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setComments(e.getMessage());
            rcsMessageRequestRepository.save(rcsMessageRequest);
        }

    }

    private String validateNumber(String number) {
        if (number == null) return null;
        number = number.replaceAll("[\\s-]", "");
        if (number.startsWith("+")) {
            number = number.substring(1);
        }
        if (number.length() == 10) {
            if (number.matches("[6-9][0-9]{9}")) {
                return "91" + number;
            } else {
                return null;
            }
        } else if (number.length() == 12 && number.startsWith("91")) {
            String lastTen = number.substring(2);
            if (lastTen.matches("[6-9][0-9]{9}")) {
                return number;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Segregate the supplied recipient numbers into valid (normalized) and invalid lists.
     * Uses validateNumber(...) to normalize and validate each input.
     * Returns a RecipientValidationResult containing both lists (never null).
     */
    private static record RecipientValidationResult(List<String> validNumbers, List<String> invalidNumbers) {
    }

    private RecipientValidationResult validateRecipients(List<String> recipients) {
        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        if (recipients == null || recipients.isEmpty()) {
            return new RecipientValidationResult(valid, invalid);
        }
        for (String recipient : recipients) {
            String normalized = validateNumber(recipient);
            if (normalized != null) {
                valid.add(normalized);
            } else {
                invalid.add(recipient);
            }
        }
        return new RecipientValidationResult(valid, invalid);
    }

    private Object parseContent(Object content, RcsContentType contentType) throws JsonProcessingException {
        String contentStr = (String) content;
        if (contentType == RcsContentType.PLAIN_TEXT) {
            return objectMapper.readValue(contentStr, RcsPlainText.class);
        } else if (contentType == RcsContentType.RICH_CARD) {
            return objectMapper.readValue(contentStr, SingleRichCard.class);
        } else if (contentType == RcsContentType.CAROUSEL) {
            return objectMapper.readValue(contentStr, MultipleRichCard.class);
        }
        return content;
    }

    private Object formPersonalizedContentObject(Object personalizedParsedTemplate, RcsContentType contentType, Map<String, String> personalizedMap) throws JsonProcessingException {
        Object personalizedContent = null;
        if (personalizedParsedTemplate instanceof RcsPlainText plainText) {
            String filledText = fillTemplateWithMap(plainText.getText(), personalizedMap);
            plainText.setText(filledText);
            personalizedContent = plainText;
        } else if (personalizedParsedTemplate instanceof SingleRichCard richCard) {
            CardContent content = richCard.getContent();
            if (content != null) {
                if (content.getCardTitle() != null) {
                    content.setCardTitle(fillTemplateWithMap(content.getCardTitle(), personalizedMap));
                }
                if (content.getCardDescription() != null) {
                    content.setCardDescription(fillTemplateWithMap(content.getCardDescription(), personalizedMap));
                }
            }
            personalizedContent = richCard;
        } else if (personalizedParsedTemplate instanceof MultipleRichCard carousel) {
            if (carousel.getContents() != null) {
                for (CardContent content : carousel.getContents()) {
                    if (content.getCardTitle() != null) {
                        content.setCardTitle(fillTemplateWithMap(content.getCardTitle(), personalizedMap));
                    }
                    if (content.getCardDescription() != null) {
                        content.setCardDescription(fillTemplateWithMap(content.getCardDescription(), personalizedMap));
                    }
                }
            }
            personalizedContent = carousel;
        }
        return personalizedContent;
    }

    public String fillTemplateWithMap(String template, Map<String, String> values) {
        if (template == null || values == null) return template;
        String result = template;
        log.info("Filling template: {} {}", template, values);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String placeholder = "{{" + entry.getKey().toLowerCase() + "}}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }

}
