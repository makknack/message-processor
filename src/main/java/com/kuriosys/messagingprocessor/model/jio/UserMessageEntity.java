package com.kuriosys.messagingprocessor.model.jio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMessageEntity {
    private String location;
    private String messageId;
    private String referenceID;
    private OffsetDateTime sendTime;
    private SuggestionResponse suggestionResponse;
    private String text;
    private String userFile;
}

