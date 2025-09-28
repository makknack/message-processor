package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// SEND , READ Delivered and failed status of message

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JioWebhookEvent {
    private String agentId;
    private String entityType;
    private String userPhoneNumber;
    private Entity entity;
    private MetaData metaData;
}
