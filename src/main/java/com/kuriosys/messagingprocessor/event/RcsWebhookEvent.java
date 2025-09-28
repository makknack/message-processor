package com.kuriosys.messagingprocessor.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class RcsWebhookEvent extends Event {
    private String payload;
}
