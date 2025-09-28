package com.kuriosys.messagingprocessor.util;

import com.kuriosys.messagingprocessor.enums.RcsWebhookEventTypes;

import java.util.HashMap;
import java.util.Map;

public class EventTypeConverter {
    private static final Map<String, RcsWebhookEventTypes> EXTERNAL_TO_SYSTEM_EVENT_MAP = new HashMap<>();

    static {
        EXTERNAL_TO_SYSTEM_EVENT_MAP.put("MESSAGE_READ", RcsWebhookEventTypes.READ);
        EXTERNAL_TO_SYSTEM_EVENT_MAP.put("MESSAGE_DELIVERED", RcsWebhookEventTypes.DELIVERED);
        EXTERNAL_TO_SYSTEM_EVENT_MAP.put("SEND_MESSAGE_SUCCESS", RcsWebhookEventTypes.SENT);
        EXTERNAL_TO_SYSTEM_EVENT_MAP.put("SEND_MESSAGE_FAILURE", RcsWebhookEventTypes.SENDING_FAILED);
    }

    public static RcsWebhookEventTypes getEvent(String externalEventType) {
        return EXTERNAL_TO_SYSTEM_EVENT_MAP.getOrDefault(externalEventType, RcsWebhookEventTypes.OTHER);
    }
}

