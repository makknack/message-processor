package com.kuriosys.messagingprocessor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuriosys.messagingprocessor.enums.RcsMessageRecipientStatus;
import com.kuriosys.messagingprocessor.exception.ProcessingException;
import com.kuriosys.messagingprocessor.model.ServiceRouteDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpSenderService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public Mono<String> send(String endpoint,
                                 Map<String, Object> payload,
                                 Map<String, String> headers) {

        return webClient.post()
                .uri(endpoint)
                .headers(h -> h.setAll(headers == null ? Map.of() : headers))
                .bodyValue(payload)
                .exchangeToMono((ClientResponse response) ->
                        response.bodyToMono(String.class).defaultIfEmpty("")
                                .map(responseBody -> responseBody)
                );
    }
}
