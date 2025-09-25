package com.kuriosys.messagingprocessor.enums;

public enum RcsMessageRequestStatus {
    PENDING(0),
    PROCESSING(1),
    COMPLETED(2),
    FAILED(3);

    private final int value;

    RcsMessageRequestStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}