package com.example.order.model.outbox;

public enum OutboxStatus {
    NEW,
    RETRY,
    PUBLISHED,
    DEAD
}
