package com.kuriosys.messagingprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RcsResponseEvent {
    private String source;
    private String eventType;
    private String payload;
    private OffsetDateTime sentAt;
}

