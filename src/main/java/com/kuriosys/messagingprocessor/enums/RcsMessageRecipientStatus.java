package com.kuriosys.messagingprocessor.enums;

public enum RcsMessageRecipientStatus {

    PENDING(0),
    PROCESSING(1),
    SENT(2),
    SENDING_FAILED(3);

    private final int value;

    RcsMessageRecipientStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

