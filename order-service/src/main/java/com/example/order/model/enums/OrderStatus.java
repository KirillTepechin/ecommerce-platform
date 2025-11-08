package com.example.order.model.enums;

public enum OrderStatus {
    PENDING,        // Заказ создан, ожидает подтверждения
    CONFIRMED,      // Заказ подтвержден магазином
    PAID,           // Заказ оплачен (может быть автоматически после подтверждения)
    CANCELLED,      // Заказ отменен (клиентом или магазином)
    PROCESSING,     // Заказ в обработке
    SHIPPED,        // Заказ отправлен
    DELIVERED,      // Заказ доставлен
    REFUNDED        // Возврат средств
}
