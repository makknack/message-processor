package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSendingService {

    private final HttpSenderService httpSenderService;
    private final RecipientPersistenceService recipientPersistenceService;
    private final ObjectMapper objectMapper;

    /**
     * Sends payload using the provided PayloadCreator and handler, persists results, and returns the raw response body.
     * Reactive: returns Mono<String> with the provider response. On error, persists failure and propagates the error.
     */
    public Mono<String> sendAndPersist(PayloadRequest request,
                                       PayloadCreator payloadCreator,
                                       ServiceProviderApiResponseHandler responseHandler) throws JsonProcessingException {
        if (request == null) return Mono.error(new ProcessingException("PayloadRequest cannot be null"));

        Map<String, Object> requestBody;
        try {
            requestBody = payloadCreator.createPayload(request);
        } catch (Exception e) {
            return Mono.error(new ProcessingException("Failed to create payload: " + e.getMessage()));
        }

        ServiceRouteDetails route = request.getServiceRouteDetails();
        if (route == null || route.getBaseUrl() == null) {
            return Mono.error(new ProcessingException("Service route or URL missing in request"));
        }

        String endpoint = route.getBaseUrl();
        String endpointPath = route.getEndpoint();
        if (endpointPath != null && !endpointPath.isEmpty()) endpoint = endpoint + endpointPath;

        final String requestJson = objectMapper.writeValueAsString(requestBody);

        List<String> recipients = request.getRecipients();
        Object personalizedContent = request.isPersonalized() ? (request.getPersonalizedValues() != null ? request.getPersonalizedValues() : request.getContent()) : null;

        // perform send
        return httpSenderService.send(endpoint, requestBody, payloadCreator.getHeaders(request))
                .flatMap(responseBody -> {
                    String referenceId = null;

                    try {
                        referenceId = responseHandler != null ? responseHandler.handle(responseBody) : null;
                    } catch (Exception e) {
                        log.warn("Response handler failed for messageRequestId {}: {}", request.getMessageRequestId(), e.getMessage());
                    }
                    return recipientPersistenceService.persistSuccess(
                            request.getUserId(), request.getMessageRequestId(), recipients, personalizedContent, referenceId, responseBody, requestJson
                    ).thenReturn(responseBody);
                })
                .onErrorResume(ex -> {
                    return recipientPersistenceService.persistFailure(
                            request.getUserId(), request.getMessageRequestId(), recipients, personalizedContent, ex, requestJson
                    ).then(Mono.error(ex));
                });
    }

    /**
     * Synchronous wrapper for legacy callers. Blocks and returns provider response string or throws.
     */
    public String sendAndPersistSync(PayloadRequest request, PayloadCreator payloadCreator, ServiceProviderApiResponseHandler responseHandler) throws ProcessingException {
        try {
            return sendAndPersist(request, payloadCreator, responseHandler).block();
        } catch (RuntimeException | JsonProcessingException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof ProcessingException) throw (ProcessingException) cause;
            throw new ProcessingException(cause.getMessage());
        }
    }
}

