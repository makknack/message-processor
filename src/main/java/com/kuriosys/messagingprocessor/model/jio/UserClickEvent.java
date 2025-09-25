package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserClickEvent {
    private String agentId;
    private String entityType;
    private String userPhoneNumber;
    private MetaData metaData;
    private UserMessageEntity entity;
}
