package com.kuriosys.messagingprocessor.enums;

import lombok.Getter;

@Getter
public enum EventType {

    RCS_FILE_MSG_REQ("RFMSGREQ"),
    RCS_GROUP_MSG_REQ("RGMSGREQ"),
    RCS_NUMBER_MSG_REQ("RNMSGREQ"),
    RCS_MSG_SEND("RCS_MSG_SEND"),
    RCS_MSG_DELIVERED("RCS_MSG_DELIVERED"),
    RCS_MSG_READ("RCS_MSG_READ"),
    RCS_MSG_FAILED("RCS_MSG_FAILED"),
    RCS_MSG_REPLY("RCS_MSG_REPLY"),
    RCS_REQUEST_SEND("RCS_REQUEST_SEND");

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
