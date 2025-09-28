package com.kuriosys.messagingprocessor.event;

import lombok.Data;

@Data
public class Event {
    private String eventId;
    private String eventType;
    private String source;
}
