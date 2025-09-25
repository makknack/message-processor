package com.kuriosys.messagingprocessor.service;

import com.kuriosys.messagingprocessor.repository.RcsMessageRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RcsHttpApiService {
    private final Logger log = LoggerFactory.getLogger(RcsHttpApiService.class);
    private final WebClient webClient;
    private final RcsMessageRecipientRepository rcsMessageRecipientRepository;

    /**
     * Send a batch of recipients to the given endpoint and insert results into DB.
     * @param recipients List of RcsMessageRecipient to send
     * @param endpoint API endpoint URL
     * @param payload Additional payload (if needed)
     */

}

