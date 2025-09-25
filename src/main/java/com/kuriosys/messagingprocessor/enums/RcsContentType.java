package com.kuriosys.messagingprocessor.enums;

public enum RcsContentType {
    PLAIN_TEXT(0),
    RICH_CARD(1),
    CAROUSEL(2);

    private final int value;

    RcsContentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}