package com.kuriosys.messagingprocessor.service;

import com.kuriosys.messagingprocessor.enums.RcsMessageRecipientStatus;
import com.kuriosys.messagingprocessor.model.RcsMessageRecipient;
import com.kuriosys.messagingprocessor.repository.RcsMessageRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RcsMessageRecipientService {

    private final RcsMessageRecipientRepository rcsMessageRecipientRepository;

    private RcsMessageRecipient populateRcsMessageRecipient(BigInteger userId, String messageRequestId, String recipient, RcsMessageRecipientStatus status, Object personalizedContent, String referenceId, String request, String response, String comment) {
        RcsMessageRecipient rcsMessageRecipient = new RcsMessageRecipient();
        rcsMessageRecipient.setMessageRequestId(messageRequestId);
        rcsMessageRecipient.setRecipient(recipient);
        rcsMessageRecipient.setStatus(status);
        rcsMessageRecipient.setUserId(userId);
        rcsMessageRecipient.setPersonalizedContent(personalizedContent);
        rcsMessageRecipient.setRequest(request);
        rcsMessageRecipient.setResponse(response);
        rcsMessageRecipient.setReferenceId(referenceId);
        rcsMessageRecipient.setComments(comment);
        rcsMessageRecipient.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        rcsMessageRecipient.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return rcsMessageRecipient;
    }

    public void saveRcsMessageRecipient(BigInteger userId, String messageRequestId, List<String> recipients , RcsMessageRecipientStatus status, Object personalizedContent, String referenceId, String request, String response,String comment){
        List<RcsMessageRecipient> rcsMessageRecipients = new ArrayList<>();
        for (String recipient: recipients){
             rcsMessageRecipients.add(populateRcsMessageRecipient(userId, messageRequestId, recipient, status, personalizedContent, referenceId, request, response, comment));
        }
        rcsMessageRecipientRepository.saveAll(rcsMessageRecipients);
    }

    public void insertFailedRecipients(BigInteger userId, String messageRequestId, List<String> recipients, RcsMessageRecipientStatus status){
        List<RcsMessageRecipient> rcsMessageRecipients = new ArrayList<>();
        recipients.forEach(
                recipient -> rcsMessageRecipients.add(
                        populateRcsMessageRecipient(userId, messageRequestId, recipient, status, null, null, null, null, null)
                )
        );
        rcsMessageRecipientRepository.saveAll(rcsMessageRecipients);
    }
}
