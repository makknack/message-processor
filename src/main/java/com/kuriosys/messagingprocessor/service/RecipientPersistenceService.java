package com.kuriosys.messagingprocessor.service;

import com.kuriosys.messagingprocessor.enums.RcsMessageRecipientStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipientPersistenceService {

    private final RcsMessageRecipientService rcsMessageRecipientService;

    public Mono<Void> persistSuccess(BigInteger userId,
                                     String messageRequestId,
                                     List<String> recipients,
                                     Object personalizedContent,
                                     String referenceId,
                                     String responseBody,
                                     String requestJson) {
        return Mono.fromRunnable(() -> rcsMessageRecipientService.saveRcsMessageRecipient(
                userId, messageRequestId, recipients,
                RcsMessageRecipientStatus.SENT,
                personalizedContent,
                referenceId,
                requestJson,
                responseBody,
                null
        )).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> persistFailure(BigInteger userId,
                                     String messageRequestId,
                                     List<String> recipients,
                                     Object personalizedContent,
                                     Throwable error,
                                     String requestJson) {
        String comment = error != null ? error.getMessage() : null;
        return Mono.fromRunnable(() -> rcsMessageRecipientService.saveRcsMessageRecipient(
                userId, messageRequestId, recipients,
                RcsMessageRecipientStatus.SENDING_FAILED,
                personalizedContent,
                null,
                requestJson,
                null,
                comment
        )).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
