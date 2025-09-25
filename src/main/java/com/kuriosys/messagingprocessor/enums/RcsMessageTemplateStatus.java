package com.kuriosys.messagingprocessor.enums;

public enum RcsMessageTemplateStatus {

    DRAFT(0),
    APPROVED(1),
    REJECTED(2),
    DELETED(3);

    private final int value;

    RcsMessageTemplateStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

