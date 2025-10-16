package com.vibepay.domain;

public enum TransactionType {
    EARN("적립"),
    USE("사용"),
    RESTORE("복구");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}