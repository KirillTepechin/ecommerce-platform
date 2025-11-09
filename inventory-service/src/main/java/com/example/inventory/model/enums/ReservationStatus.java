package com.example.inventory.model.enums;

public enum ReservationStatus {
    PENDING,      // Резервирование создано
    RESERVED,     // Товары зарезервированы
    COMMITTED,    // Резервирование подтверждено (заказ оплачен)
    CANCELLED,    // Резервирование отменено
    RELEASED      // Товары возвращены на склад
}
