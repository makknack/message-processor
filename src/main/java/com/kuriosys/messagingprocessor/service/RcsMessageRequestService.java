package com.kuriosys.messagingprocessor.service;

import com.kuriosys.messagingprocessor.enums.RcsMessageRequestStatus;
import com.kuriosys.messagingprocessor.model.RcsMessageRequest;
import com.kuriosys.messagingprocessor.repository.RcsMessageRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class RcsMessageRequestService {

    private final RcsMessageRequestRepository rcsMessageRequestRepository;

    public void updateMessageRequestSummary(RcsMessageRequest rcsMessageRequest, RcsMessageRequestStatus status,int processedRecord, int validRecipientsCount, int invalidRecipientsCount) {
        rcsMessageRequest.setProcessedRecord(rcsMessageRequest.getProcessedRecord() + processedRecord);
        rcsMessageRequest.setValidRecipients(rcsMessageRequest.getValidRecipients() + validRecipientsCount);
        rcsMessageRequest.setInvalidRecipients(rcsMessageRequest.getInvalidRecipients() + invalidRecipientsCount);
        rcsMessageRequest.setStatus(status);

        if(rcsMessageRequest.getProcessedRecord() >= rcsMessageRequest.getTotalRecipients() ||
        status == RcsMessageRequestStatus.COMPLETED) {
            rcsMessageRequest.setStatus(RcsMessageRequestStatus.COMPLETED);
            rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }

        rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        rcsMessageRequestRepository.save(rcsMessageRequest);
    }

    public void updateMessageRequestStatus(RcsMessageRequest rcsMessageRequest, RcsMessageRequestStatus status,
                                           String comments) {
        rcsMessageRequest.setStatus(status);
        if(status == RcsMessageRequestStatus.COMPLETED) {
            rcsMessageRequest.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        rcsMessageRequest.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        rcsMessageRequest.setComments(comments);
        rcsMessageRequestRepository.save(rcsMessageRequest);
    }
}
