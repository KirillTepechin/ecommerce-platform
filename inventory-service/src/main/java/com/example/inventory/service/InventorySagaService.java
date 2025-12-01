package com.example.inventory.service;

import event.InventoryReservedEvent;
import event.OrderRejectedEvent;
import event.OrderCreatedEvent;
import event.PaymentCompletedEvent;
import event.PaymentFailedEvent;
import com.example.inventory.model.enums.ReservationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventorySagaService {

    private final ProductInventoryService inventoryService;
    private final InventoryReservationService reservationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Шаг 1: Резервирование товаров при создании заказа
    @KafkaListener(topics = "order-created", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Processing inventory reservation for order: {}", event.getOrderId());

        try {
            // Пытаемся зарезервировать все товары из заказа
            boolean reservationSuccessful = reserveInventoryForOrder(event);

            if (reservationSuccessful) {
                // Публикуем событие успешного резервирования
                publishInventoryReservedEvent(event);
                log.info("Inventory reserved successfully for order: {}", event.getOrderId());
            } else {
                // Публикуем событие отказа (товара нет в наличии)
                publishOrderRejectedEvent(event, "Insufficient inventory");
                log.warn("Order rejected due to insufficient inventory: {}", event.getOrderId());
            }

        } catch (Exception e) {
            log.error("Error processing inventory for order: {}", event.getOrderId(), e);
            publishOrderRejectedEvent(event, "Inventory processing error: " + e.getMessage());
        }
    }

    // Шаг 2: Подтверждение резервирования при успешной оплате
    @KafkaListener(topics = "payment-completed", groupId = "inventory-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Committing inventory reservation for order: {}", event.getOrderId());

        try {
            reservationService.commitReservation(event.getOrderId());
            log.info("Inventory reservation committed for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error committing inventory for order: {}", event.getOrderId(), e);
            // В реальной системе здесь должна быть компенсирующая транзакция
        }
    }

    // Шаг 3: Отмена резервирования при неудачной оплате
    @KafkaListener(topics = "payment-failed", groupId = "inventory-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Cancelling inventory reservation for order: {}", event.getOrderId());

        try {
            reservationService.cancelReservation(event.getOrderId());
            log.info("Inventory reservation cancelled for order: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error cancelling inventory for order: {}", event.getOrderId(), e);
        }
    }

    private boolean reserveInventoryForOrder(OrderCreatedEvent event) {
        // Создаем резервирование в статусе PENDING
        reservationService.createPendingReservation(event.getOrderId(), event.getItems());

        try {
            // Пытаемся зарезервировать товары
            for (var item : event.getItems()) {
                boolean reserved = inventoryService.reserveProduct(
                        item.getProductId(),
                        item.getQuantity(),
                        event.getOrderId()
                );

                if (!reserved) {
                    // Если не удалось зарезервировать - откатываем все резервирования
                    rollbackReservation(event.getOrderId());
                    return false;
                }
            }

            // Все товары зарезервированы - меняем статус на RESERVED
            reservationService.updateReservationStatus(event.getOrderId(), ReservationStatus.RESERVED);
            return true;

        } catch (Exception e) {
            rollbackReservation(event.getOrderId());
            throw e;
        }
    }

    private void rollbackReservation(Long orderId) {
        log.info("Rolling back inventory reservation for order: {}", orderId);
        try {
            reservationService.cancelReservation(orderId);
        } catch (Exception e) {
            log.error("Error during reservation rollback for order: {}", orderId, e);
        }
    }

    private void publishInventoryReservedEvent(OrderCreatedEvent originalEvent) {
        final InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(originalEvent.getOrderId())
                .customerId(originalEvent.getCustomerId())
                .reservedAt(LocalDateTime.now())
                .totalAmount(originalEvent.getTotalAmount())
                .build();

        kafkaTemplate.send("inventory-reserved", event);
    }

    private void publishOrderRejectedEvent(OrderCreatedEvent originalEvent, String reason) {
        OrderRejectedEvent event = OrderRejectedEvent.builder()
                .orderId(originalEvent.getOrderId())
                .customerId(originalEvent.getCustomerId())
                .reason(reason)
                .rejectedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("order-rejected", event);
    }
}