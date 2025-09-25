package com.kuriosys.messagingprocessor.enums;

import lombok.Getter;

@Getter
public enum EventType {

    RCS_FILE_MSG_REQ("RFMSGREQ"),
    RCS_GROUP_MSG_REQ("RGMSGREQ"),
    RCS_NUMBER_MSG_REQ("RNMSGREQ"),
    RCS_SEND_EVENT("RCS_SEND"),
    RCS_DELIVERED_EVENT("RCS_DELIVERED"),
    RCS_READ_EVENT("RCS_READ"),
    RCS_FAILED_EVENT("RCS_FAILED"),
    RCS_REPLY_EVENT("RCS_REPLY"),
    RCS_REQUEST_SEND_EVENT("RCS_REQUEST_SEND");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public static EventType fromValue(String value) {
        for (EventType type : EventType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
