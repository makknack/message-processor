package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.constant.Constants;
import com.kuriosys.messagingprocessor.enums.*;
import com.kuriosys.messagingprocessor.model.*;
import com.kuriosys.messagingprocessor.repository.*;
import com.kuriosys.messagingprocessor.event.RcsSubmissionEvent;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.service.vendor.DataGPayloadCreator;
import com.kuriosys.messagingprocessor.service.vendor.JioPayloadCreator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
    private final WebClient webClient;
    private final static int INSERT_BATCH_SIZE = 49; // Batch size for inserting recipients
    private final int FETCH_SIZE = 49; // Batch size for processing contacts

    public void sendToFileNumbers(RcsSubmissionEvent rcsSubmissionEvent) throws IOException {
        RcsMessageRequest rcsMessageRequest;
        try {
            rcsMessageRequest = rcsMessageRequestRepository.findByMessageRequestId(rcsSubmissionEvent.getMessageRequestId())
                    .orElseThrow( () -> new ProcessingException("RCS Message Request not found for ID: " + rcsSubmissionEvent.getMessageRequestId()));
      
            if (rcsMessageRequest.getIsPersonalized()) {
                sendPersonalizedMessageToFileContacts(rcsMessageRequest);
            } else {
                sendBulkMessageToFileContacts(rcsMessageRequest);
            }
        } catch (ProcessingException e) {
            log.warn("Processing exception occurred: {}", e.getMessage());
        }
    }

    public void sendToGroupNumbers(RcsSubmissionEvent rcsSubmissionEvent) throws IOException {
        Optional<RcsMessageRequest> rcsMessageRequestOptional;
        try {
            rcsMessageRequestOptional = rcsMessageRequestRepository.findByMessageRequestId(rcsSubmissionEvent.getMessageRequestId());
            if (rcsMessageRequestOptional.isEmpty()) {
                throw new ProcessingException("RCS Message Request not found for ID");
            }
            RcsMessageRequest rcsMessageRequest = rcsMessageRequestOptional.get();
            if (rcsMessageRequest.getIsPersonalized()) {
                sendPersonalizedMessagesToGroup(rcsMessageRequest);
            } else {
                sendBulkMessagesToGroup(rcsMessageRequest);
            }
        } catch (ProcessingException e) {
            log.warn("Processing exception occurred: {}", e.getMessage());
        }
    }

    public void sendToManualNumbers(RcsSubmissionEvent rcsSubmissionEvent) {
        RcsMessageRequest rcsMessageRequest = null;
        int sendBatchSize = 49;
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

            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));
            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();

            PayloadCreator payloadCreator = getPayloadCreator(serviceRouteDetails, rcsMessageRequest, rcsExternalAgent);

            final List<RcsMessageRecipient> validRcsMessageRecipients = new ArrayList<>();
            final List<RcsMessageRecipient> inValidRcsMessageRecipients = new ArrayList<>();
            final List<String> validRecipients = new ArrayList<>();

            long processedRecord = rcsMessageRequest.getProcessedRecord();
            long validRecipientsCount = rcsMessageRequest.getValidRecipients();
            long invalidRecipientsCount = rcsMessageRequest.getInvalidRecipients();
            if (rcsMessageRequest.getIsPersonalized()) {
                for (int i = 0; i < recipients.size(); i++) {
                        String recipient = recipients.get(i);
                        String validRecipient =  validateNumber(recipient);
                        if(validRecipient != null){
                            Object templateContentObject = parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType());
                            Map<String, String> personalizedDetailsMap = rcsSubmissionEvent.getTemplateVariables().get(i);
                            log.debug("personalizedDetailsMap  {}", personalizedDetailsMap);
                            Object personalizedContentObject = formPersonalizedContentObject(templateContentObject, personalizedDetailsMap);
                            String personalizedContent = objectMapper.writeValueAsString(personalizedContentObject);
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, personalizedContent, null);
                            Map<String, Object> requestBody = payloadCreator.createPayload(personalizedContentObject, List.of(validRecipient));
                            Map<String, String> additionalHeaders = payloadCreator.getHeaders();
                            sendAndInsert(url, requestBody, additionalHeaders, List.of(rcsMessageRecipient));
                            validRecipientsCount++;
                      } else {
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                            inValidRcsMessageRecipients.add(rcsMessageRecipient);
                            invalidRecipientsCount++;
                    }
                    processedRecord++;
                }
                if (!inValidRcsMessageRecipients.isEmpty()) {
                    rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                    inValidRcsMessageRecipients.clear();
                }
                rcsMessageRequest.setProcessedRecord(processedRecord);
                rcsMessageRequest.setValidRecipients(validRecipientsCount);
                rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
                rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
                rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
                rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                rcsMessageRequestRepository.save(rcsMessageRequest);
            } else {
                Object contentObject = parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType());
                Map<String, String> headers = payloadCreator.getHeaders();
                do {
                    if (recipients.size() < sendBatchSize) {
                        sendBatchSize = recipients.size();
                    }
                    List<String> recipientsBatch = recipients.subList(0, sendBatchSize);
                    recipients = recipients.subList(sendBatchSize, recipients.size());
                    for (String recipient : recipientsBatch) {
                        String validRecipient =  validateNumber(recipient);
                        if (validRecipient != null) {
                            validRecipients.add(validRecipient);
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, null, null);
                            validRcsMessageRecipients.add(rcsMessageRecipient);
                            validRecipientsCount++;
                        } else {
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                            inValidRcsMessageRecipients.add(rcsMessageRecipient);
                            invalidRecipientsCount++;
                        }
                        processedRecord++;
                    }

                    if (!inValidRcsMessageRecipients.isEmpty()) {
                        rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                        inValidRcsMessageRecipients.clear();
                    }
                    if (!validRecipients.isEmpty()) {
                        Map<String, Object> payload = payloadCreator.createPayload(contentObject, validRecipients);
                        sendAndInsert(url, payload, headers, new ArrayList<>(validRcsMessageRecipients));
                        validRcsMessageRecipients.clear();
                    }
                } while (!recipients.isEmpty());
                rcsMessageRequest.setProcessedRecord(processedRecord);
                rcsMessageRequest.setValidRecipients(validRecipientsCount);
                rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
                rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
                rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
                rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                rcsMessageRequestRepository.save(rcsMessageRequest);
            }
        } catch (ProcessingException | JsonProcessingException e) {
            log.error("Processing exception occurred: {}", rcsSubmissionEvent.getMessageRequestId(), e);
            if (rcsMessageRequest != null) {
                rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
                rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                rcsMessageRequest.setComments(e.getMessage());
                rcsMessageRequestRepository.save(rcsMessageRequest);
            }
        }
    }

    private static PayloadCreator getPayloadCreator(ServiceRouteDetails serviceRouteDetails, RcsMessageRequest rcsMessageRequest, RcsExternalAgent rcsExternalAgent) throws ProcessingException {
        PayloadCreator payloadCreator;
        if(serviceRouteDetails.getServiceProvider().equalsIgnoreCase(ServiceProvider.JIO.name())){
            payloadCreator = new JioPayloadCreator(
                    rcsMessageRequest, serviceRouteDetails, rcsExternalAgent
            );
        }
        else if(serviceRouteDetails.getServiceProvider().equalsIgnoreCase(ServiceProvider.DATAG.name())){
            payloadCreator = new DataGPayloadCreator(
                    rcsMessageRequest, serviceRouteDetails, rcsExternalAgent
            );
        }
        else{
            throw new ProcessingException("Invalid service provider " + serviceRouteDetails.getServiceProvider());
        }
        return payloadCreator;
    }

    private void sendPersonalizedMessageToFileContacts(RcsMessageRequest rcsMessageRequest) throws ProcessingException, IOException {
        try {
            int sendBatchSize = 1;
            CsvReaderService csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, sendBatchSize);
            Optional<RcsMessageTemplate> rcsMessageTemplateOpt = rcsMessageTemplateRepository.findById(rcsMessageRequest.getMessageTemplateId());
            if (rcsMessageTemplateOpt.isEmpty()) {
                throw new ProcessingException("Message template not found " + rcsMessageRequest.getMessageTemplateId());
            }
            RcsMessageTemplate rcsMessageTemplate = rcsMessageTemplateOpt.get();

            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));
            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();

            PayloadCreator payloadCreator = getPayloadCreator(rcsMessageRequest, serviceRouteDetails, rcsExternalAgent);

            List<RcsMessageRecipient> inValidRcsMessageRecipients = new ArrayList<>();

            long processedRecord = rcsMessageRequest.getProcessedRecord();
            long validRecipientsCount = rcsMessageRequest.getValidRecipients();
            long invalidRecipientsCount = rcsMessageRequest.getInvalidRecipients();
            Object templateContentObject = parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType());
            while (csvReaderService.hasMoreRecords()) {
                Map<String, String> nextRecordMap = csvReaderService.getNextRecordAsMap();
                String recipient = nextRecordMap.get(Constants.FILE_HEADER_RECIPIENTS);
                String validRecipient =  validateNumber(recipient);
                if (validRecipient!= null) {
                    Object personalizedContentObject = formPersonalizedContentObject(templateContentObject, nextRecordMap);
                    String personalizedContent = objectMapper.writeValueAsString(personalizedContentObject);
                    RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, personalizedContent, null);
                    Map<String, Object> requestBody = payloadCreator.createPayload(personalizedContentObject, List.of(validRecipient));
                    Map<String, String> headers = payloadCreator.getHeaders();
                    sendAndInsert(url, requestBody, headers, List.of(rcsMessageRecipient));
                    validRecipientsCount++;
                } else {
                    RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                    inValidRcsMessageRecipients.add(rcsMessageRecipient);
                    invalidRecipientsCount++;
                }
                processedRecord++;
                if (!inValidRcsMessageRecipients.isEmpty() && inValidRcsMessageRecipients.size() >= INSERT_BATCH_SIZE) {
                    rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                    inValidRcsMessageRecipients.clear();
                }
            }
            if (!inValidRcsMessageRecipients.isEmpty()) {
                rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                inValidRcsMessageRecipients.clear();
            }
            rcsMessageRequest.setProcessedRecord(processedRecord);
            rcsMessageRequest.setValidRecipients(validRecipientsCount);
            rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
            rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequestRepository.save(rcsMessageRequest);
        } catch (ProcessingException | JsonProcessingException e) {
            log.error("Processing exception occurred", e);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setComments(e.getMessage());
            rcsMessageRequestRepository.save(rcsMessageRequest);
        }
    }

    private static PayloadCreator getPayloadCreator(RcsMessageRequest rcsMessageRequest, ServiceRouteDetails serviceRouteDetails, RcsExternalAgent rcsExternalAgent) throws ProcessingException {
        PayloadCreator payloadCreator;
        if(serviceRouteDetails.getServiceProvider().equalsIgnoreCase(ServiceProvider.JIO.name())){
            if(serviceRouteDetails.getApiKey() == null) {
                throw new ProcessingException("ServiceRouteDetails or API Key is not set");
            }
            payloadCreator = new JioPayloadCreator(
                    rcsMessageRequest, serviceRouteDetails, rcsExternalAgent
            );
        }
        else{
            throw new ProcessingException("Invalid service provider " + serviceRouteDetails.getServiceProvider());
        }
        return payloadCreator;
    }

    private void sendBulkMessageToFileContacts(RcsMessageRequest rcsMessageRequest) {
        try {
            int sendBatchSize = 49;
            CsvReaderService csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, sendBatchSize);

            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));

            PayloadCreator payloadCreator = getPayloadCreator(rcsMessageRequest, serviceRouteDetails, rcsExternalAgent);

            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();

            long processedRecord = rcsMessageRequest.getProcessedRecord();
            long validRecipientsCount = rcsMessageRequest.getValidRecipients();
            long invalidRecipientsCount = rcsMessageRequest.getInvalidRecipients();

            Object contentObject = parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType());
            List<RcsMessageRecipient> inValidRcsMessageRecipients = new ArrayList<>();
            List<RcsMessageRecipient> validRcsMessageRecipients = new ArrayList<>();
            List<String> validRecipients = new ArrayList<>();
            Map<String, String> headers = payloadCreator.getHeaders();
            while (csvReaderService.hasMoreRecords()) {
                List<String> recipients = csvReaderService.getNextBatchOfFirstColumn(FETCH_SIZE);
                if (recipients == null || recipients.isEmpty()) {
                    break;
                }
                do {
                    if (recipients.size() < sendBatchSize) {
                        sendBatchSize = recipients.size();
                    }
                    List<String> recipientBatch = recipients.subList(0, sendBatchSize);
                    recipients = recipients.subList(sendBatchSize, recipients.size());
                    for (String recipient : recipientBatch) {
                        String validRecipient =  validateNumber(recipient);
                        if (validRecipient != null) {
                            validRecipients.add(validRecipient);
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, null, null);
                            validRcsMessageRecipients.add(rcsMessageRecipient);
                        } else {
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                            inValidRcsMessageRecipients.add(rcsMessageRecipient);
                        }
                    }

                    processedRecord += recipientBatch.size();
                    validRecipientsCount += validRcsMessageRecipients.size();
                    invalidRecipientsCount += inValidRcsMessageRecipients.size();

                    if (!inValidRcsMessageRecipients.isEmpty()) {
                        rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                        inValidRcsMessageRecipients.clear();
                    }

                    if (!validRecipients.isEmpty()) {
                        Map<String, Object> payload = payloadCreator.createPayload(contentObject, validRecipients);
                        sendAndInsert(url, payload, headers, new ArrayList<>(validRcsMessageRecipients));
                        validRcsMessageRecipients.clear();
                        validRecipients.clear();
                    }

                    rcsMessageRequest.setProcessedRecord(processedRecord);
                    rcsMessageRequest.setValidRecipients(validRecipientsCount);
                    rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
                    rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    rcsMessageRequestRepository.save(rcsMessageRequest);
                } while (!recipients.isEmpty());
            }
        } catch (ProcessingException | IOException e) {
            log.error("Processing exception occurred", e);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setComments(e.getMessage());
            rcsMessageRequestRepository.save(rcsMessageRequest);
        }
    }

    private void sendPersonalizedMessagesToGroup(RcsMessageRequest rcsMessageRequest) throws ProcessingException, IOException {
        try {
            int sendBatchSize = 1;
            CsvReaderService csvReaderService = new CsvReaderService(rcsMessageRequest.getFilePath(), true, sendBatchSize);
            RcsMessageTemplate rcsMessageTemplate = rcsMessageTemplateRepository.findById(rcsMessageRequest.getMessageTemplateId())
                    .orElseThrow(() -> new ProcessingException("Message template not found"));

            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));

            PayloadCreator payloadCreator = getPayloadCreator(rcsMessageRequest, serviceRouteDetails, rcsExternalAgent);

            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();

            List<RcsMessageRecipient> inValidRcsMessageRecipients = new ArrayList<>();

            long processedRecord = rcsMessageRequest.getProcessedRecord();
            long validRecipientsCount = rcsMessageRequest.getValidRecipients();
            long invalidRecipientsCount = rcsMessageRequest.getInvalidRecipients();

            long totalFileRecords = csvReaderService.getTotalRecords();
            long totalContacts = contactRepository.countByGroupId(rcsMessageRequest.getContactGroupId());

            if (totalContacts != totalFileRecords) {
                log.warn("Mismatch: contacts in group ({}) != records in file ({}) for messageRequestId {}", totalContacts, totalFileRecords, rcsMessageRequest.getMessageRequestId());
                throw new ProcessingException("Number of contacts in group does not match number of records in file");
            }

            if (processedRecord >= totalFileRecords) {
                throw new ProcessingException("All records already processed for messageRequestId");
            }
            boolean hasMore = true;
            long currentOffset = processedRecord;
            Map<String, String> additionalHeaders = payloadCreator.getHeaders();
            while (hasMore) {
                List<String> recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, FETCH_SIZE);
                if (recipients == null || recipients.isEmpty()) {
                    break;
                }
                for (String recipient : recipients) {
                    String validRecipient =  validateNumber(recipient);
                    if (validRecipient != null) {
                        Map<String, String> nextRecordMap = csvReaderService.getNextRecordAsMap();
                        Object templateContentObject = parseContent(rcsMessageTemplate.getTemplateContent(), rcsMessageTemplate.getContentType());
                        Object personalizedContentObject = formPersonalizedContentObject(templateContentObject, nextRecordMap);
                        String personalizedContent = objectMapper.writeValueAsString(personalizedContentObject);
                        log.debug("personalizedContentObject  {}, {}", personalizedContentObject, nextRecordMap);
                        RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, personalizedContent, null);
                        Map<String, Object> requestBody = payloadCreator.createPayload(personalizedContentObject, List.of(validRecipient));
                        sendAndInsert(url, requestBody, additionalHeaders, List.of(rcsMessageRecipient));
                        validRecipientsCount++;
                    } else {
                        RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                        inValidRcsMessageRecipients.add(rcsMessageRecipient);
                        invalidRecipientsCount++;
                    }
                    processedRecord++;
                    if (!inValidRcsMessageRecipients.isEmpty() && inValidRcsMessageRecipients.size() >= INSERT_BATCH_SIZE) {
                        rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                        inValidRcsMessageRecipients.clear();
                    }
                }
                if (recipients.size() < FETCH_SIZE) {
                    hasMore = false;
                }
                currentOffset = processedRecord;
            }
            if (!inValidRcsMessageRecipients.isEmpty()) {
                rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
            }
            rcsMessageRequest.setProcessedRecord(processedRecord);
            rcsMessageRequest.setValidRecipients(validRecipientsCount);
            rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
            rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequestRepository.save(rcsMessageRequest);
        } catch (ProcessingException e) {
            log.error("Processing exception occurred", e);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setComments(e.getMessage());
            rcsMessageRequestRepository.save(rcsMessageRequest);
        }

    }

    private void sendBulkMessagesToGroup(RcsMessageRequest rcsMessageRequest) {
        try {
            int sendBatchSize = 49;
            ServiceRouteDetails serviceRouteDetails = serviceRouteDetailsRepository.findServiceRouteDetailsByUserIdAndType(rcsMessageRequest.getUserId(), ServiceRouteType.RCS)
                    .orElseThrow(() -> new ProcessingException("Service route details not found"));

            RcsExternalAgent rcsExternalAgent = rcsExternalAgentRepository.findByAgentIdAndExternalAgentStatus(rcsMessageRequest.getRcsAgentId(), RcsExternalAgentStatus.ACTIVE)
                    .orElseThrow(() -> new ProcessingException("RCS Agent not found/Inactive"));
            PayloadCreator payloadCreator = getPayloadCreator(rcsMessageRequest, serviceRouteDetails, rcsExternalAgent);

            String url = serviceRouteDetails.getBaseUrl() + serviceRouteDetails.getEndpoint();

            long processedRecord = rcsMessageRequest.getProcessedRecord();
            long validRecipientsCount = rcsMessageRequest.getValidRecipients();
            long invalidRecipientsCount = rcsMessageRequest.getInvalidRecipients();

            Object contentObject = parseContent(rcsMessageRequest.getContent(), rcsMessageRequest.getContentType());
            List<RcsMessageRecipient> inValidRcsMessageRecipients = new ArrayList<>();
            List<RcsMessageRecipient> validRcsMessageRecipients = new ArrayList<>();
            List<String> validRecipients = new ArrayList<>();
            Map<String, String> headers = payloadCreator.getHeaders();

            long currentOffset = processedRecord;
            while (true) {
                List<String> recipients = contactRepository.findNumbersByGroupIdOrdered(rcsMessageRequest.getContactGroupId(), currentOffset, FETCH_SIZE);
                if (recipients == null || recipients.isEmpty()) {
                    break;
                }
                do {
                    if (recipients.size() < sendBatchSize) {
                        sendBatchSize = recipients.size();
                    }
                    List<String> recipientBatch = recipients.subList(0, sendBatchSize);
                    recipients = recipients.subList(sendBatchSize, recipients.size());
                    for (String recipient : recipientBatch) {
                        String validRecipient =  validateNumber(recipient);
                        if (validRecipient != null) {
                            validRecipients.add(validRecipient);
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(),rcsMessageRequest.getMessageRequestId(), validRecipient, RcsMessageRecipientStatus.PENDING, null, null);
                            validRcsMessageRecipients.add(rcsMessageRecipient);
                        } else {
                            RcsMessageRecipient rcsMessageRecipient = populateRcsMessageRecipient(rcsMessageRequest.getUserId(), rcsMessageRequest.getMessageRequestId(), recipient, RcsMessageRecipientStatus.SENDING_FAILED, null, Constants.ERR_INVALID_NUMBER);
                            inValidRcsMessageRecipients.add(rcsMessageRecipient);
                        }
                    }

                    processedRecord += recipientBatch.size();
                    validRecipientsCount += validRcsMessageRecipients.size();
                    invalidRecipientsCount += inValidRcsMessageRecipients.size();

                    if (!inValidRcsMessageRecipients.isEmpty()) {
                        rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                        inValidRcsMessageRecipients.clear();
                    }

                    if (!validRecipients.isEmpty()) {
                        Map<String, Object> payload = payloadCreator.createPayload(contentObject, validRecipients);
                        sendAndInsert(url, payload, headers, new ArrayList<>(validRcsMessageRecipients));
                        validRcsMessageRecipients.clear();
                        validRecipients.clear();
                    }

                    rcsMessageRequest.setProcessedRecord(processedRecord);
                    rcsMessageRequest.setValidRecipients(validRecipientsCount);
                    rcsMessageRequest.setInvalidRecipients(invalidRecipientsCount);
                    rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    rcsMessageRequestRepository.save(rcsMessageRequest);
                } while (!recipients.isEmpty());
                currentOffset = processedRecord;
            }
            if (!inValidRcsMessageRecipients.isEmpty()) {
                rcsMessageRecipientRepository.saveAll(inValidRcsMessageRecipients);
                inValidRcsMessageRecipients.clear();
            }

            rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
            rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequestRepository.save(rcsMessageRequest);
        } catch (ProcessingException | JsonProcessingException e) {
            log.error("Processing exception occurred", e);
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.FAILED);
            rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            rcsMessageRequest.setComments(e.getMessage());
            rcsMessageRequestRepository.save(rcsMessageRequest);
        }
    }

    private static RcsMessageRecipient populateRcsMessageRecipient(BigInteger userId, String messageRequestId, String recipient, RcsMessageRecipientStatus status, String personalizedContent, String comment) {
        RcsMessageRecipient rcsMessageRecipient = new RcsMessageRecipient();
        rcsMessageRecipient.setMessageRequestId(messageRequestId);
        rcsMessageRecipient.setRecipient(recipient);
        rcsMessageRecipient.setStatus(status);
        rcsMessageRecipient.setUserId(userId);
        rcsMessageRecipient.setPersonalizedContent(personalizedContent);
        if (comment != null) {
            rcsMessageRecipient.setComments(comment);
        }
        return rcsMessageRecipient;
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

    private Object formPersonalizedContentObject(Object templateObject, Map<String, String> personalizedMap) throws JsonProcessingException {
        Object personalizedContent = null;
        if (templateObject instanceof RcsPlainText plainText) {
            String filledText = fillTemplateWithMap(plainText.getText(), personalizedMap);
            plainText.setText(filledText);
            personalizedContent = plainText;
        } else if (templateObject instanceof SingleRichCard richCard) {
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
        } else if (templateObject instanceof MultipleRichCard carousel) {
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

    // Returns normalized 12-digit Indian number (91XXXXXXXXXX) if valid, else null
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
        // Not a valid Indian number format
        return null;
    }

    private void sendAndInsert(String url, Map<String, Object> requestBody, Map<String, String> additionalHeaders, List<RcsMessageRecipient> rcsMessageRecipients) throws ProcessingException, JsonProcessingException {
        webClient.post()
                .uri(url)
                .headers(httpHeaders -> {
                    if (!additionalHeaders.isEmpty()) {
                        additionalHeaders.forEach(httpHeaders::add);
                    }
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseStr -> {
                    String referenceId = null;
                    String request = null;
                    try {
                        log.debug("Received response: {}", responseStr);
                        ObjectMapper mapper = new ObjectMapper();
                        Map<?, ?> responseMap = mapper.readValue(responseStr, Map.class);
                        if (responseMap.containsKey("referenceID")) {
                            referenceId = responseMap.get("referenceID").toString();
                        }
                        request = objectMapper.writeValueAsString(requestBody);
                    } catch (Exception e) {
                        log.warn("Failed to parse response as JSON: {},{}", responseStr, e.getMessage());
                    }
                    for (RcsMessageRecipient rcsMessageRecipient : rcsMessageRecipients) {
                        rcsMessageRecipient.setReferenceId(referenceId);
                        rcsMessageRecipient.setResponse(responseStr);
                        rcsMessageRecipient.setRequest(request);
                        rcsMessageRecipient.setStatus(RcsMessageRecipientStatus.SENT);
                        rcsMessageRecipient.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    }
                    rcsMessageRecipientRepository.saveAll(rcsMessageRecipients);
                    return Mono.empty();
                })
                .onErrorResume(ex -> {
                    log.error("API call failed for messageRequestId {}", rcsMessageRecipients.getFirst().getMessageRequestId(), ex);
                    String request;
                    try {
                        request = objectMapper.writeValueAsString(requestBody);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    for (RcsMessageRecipient rcsMessageRecipient : rcsMessageRecipients) {
                        rcsMessageRecipient.setStatus(RcsMessageRecipientStatus.SENDING_FAILED);
                        rcsMessageRecipient.setResponse(ex.getMessage());
                        rcsMessageRecipient.setRequest(request);
                        rcsMessageRecipient.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    }
                    rcsMessageRecipientRepository.saveAll(rcsMessageRecipients);
                    return Mono.empty();
                }).subscribe();
    }
}
