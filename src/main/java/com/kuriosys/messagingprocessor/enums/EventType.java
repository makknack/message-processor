package com.kuriosys.messagingprocessor.enums;

public enum EventType {

    RCS_FILE_MSG_REQ("RFMSGREQ"),
    RCS_GROUP_MSG_REQ("RGMSGREQ"),
    RCS_NUMBER_MSG_REQ("RNMSGREQ");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
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
