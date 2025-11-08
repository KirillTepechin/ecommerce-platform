package com.example.payment.model.enums;

public enum PaymentStatus {
    PENDING,      // Платеж создан, ожидает обработки
    PROCESSING,   // Платеж в обработке
    COMPLETED,    // Платеж успешно завершен
    FAILED,       // Платеж не прошел
    REFUNDED,      // Деньги возвращены
}