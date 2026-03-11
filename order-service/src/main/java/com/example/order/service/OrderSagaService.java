package com.example.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import event.InventoryReservedEvent;
import event.OrderRejectedEvent;
import event.PaymentCompletedEvent;
import event.PaymentFailedEvent;
import com.example.order.model.Order;
import com.example.order.model.enums.OrderStatus;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"inventory-reserved", "order-rejected", "payment-completed", "payment-failed"}, groupId = "order-service-group")
    // TODO: Отрефакторить для более красивой обработки событий
    public void eventHandler(ConsumerRecord<String, String> record, @Header("__TypeId__") String eventType) {
        try {
            switch (eventType) {
                case "event.InventoryReservedEvent":
                    final InventoryReservedEvent inventoryReservedEvent = objectMapper.readValue(record.value(), InventoryReservedEvent.class);
                    handleInventoryReserved(inventoryReservedEvent);
                    break;
                case "event.OrderRejectedEvent":
                    final OrderRejectedEvent orderRejectedEvent = objectMapper.readValue(record.value(), OrderRejectedEvent.class);
                    handleOrderRejected(orderRejectedEvent);
                    break;
                case "event.PaymentCompletedEvent":
                    final PaymentCompletedEvent paymentCompletedEvent = objectMapper.readValue(record.value(), PaymentCompletedEvent.class);
                    handlePaymentCompleted(paymentCompletedEvent);
                    break;
                case "event.PaymentFailedEvent":
                    PaymentFailedEvent paymentFailedEvent = objectMapper.readValue(record.value(), PaymentFailedEvent.class);
                    handlePaymentFailed(paymentFailedEvent);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing event: {}", eventType, e);
        }
    }

    // Товары зарезервированы - заказ можно подтверждать
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order: {}", event.getOrderId());

        final Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        // Меняем статус на CONFIRMED - товары зарезервированы, ожидаем оплату
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} confirmed - waiting for payment", event.getOrderId());
        }
    }

    // Товара нет в наличии - отменяем заказ
    public void handleOrderRejected(OrderRejectedEvent event) {
        log.info("Order rejected: {}, reason: {}", event.getOrderId(), event.getReason());

        final Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to: {}", event.getOrderId(), event.getReason());
    }

    // Оплата прошла - заказ завершен
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Payment completed for order: {}", event.getOrderId());

        final Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order {} marked as PAID", event.getOrderId());
    }

    // Оплата не прошла - отменяем заказ
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Payment failed for order: {}", event.getOrderId());

        final Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to payment failure", event.getOrderId());
    }
}
