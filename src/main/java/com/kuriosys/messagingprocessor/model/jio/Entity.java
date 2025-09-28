package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entity {
    private String eventId;
    private String eventType;
    private String messageId;
    private String referenceID;
    private OffsetDateTime sendTime;
    private String senderPhoneNumber;
    private Error error;
    private String location;
    private SuggestionResponse suggestionResponse;
    private String text;
    private String userFile;
}
