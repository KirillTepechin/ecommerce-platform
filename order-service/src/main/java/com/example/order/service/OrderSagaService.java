package com.example.order.service;

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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderSagaService {

    private final OrderRepository orderRepository;

    // Товары зарезервированы - заказ можно подтверждать
    @KafkaListener(topics = "inventory-reserved", groupId = "order-service-group")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        // Меняем статус на CONFIRMED - товары зарезервированы, ожидаем оплату
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} confirmed - waiting for payment", event.getOrderId());
        }
    }

    // Товара нет в наличии - отменяем заказ
    @KafkaListener(topics = "order-rejected", groupId = "order-service-group")
    public void handleOrderRejected(OrderRejectedEvent event) {
        log.info("Order rejected: {}, reason: {}", event.getOrderId(), event.getReason());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to: {}", event.getOrderId(), event.getReason());
    }

    // Оплата прошла - заказ завершен
    @KafkaListener(topics = "payment-completed", groupId = "order-service-group")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Payment completed for order: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order {} marked as PAID", event.getOrderId());
    }

    // Оплата не прошла - отменяем заказ
    @KafkaListener(topics = "payment-failed", groupId = "order-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Payment failed for order: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + event.getOrderId()));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled due to payment failure", event.getOrderId());
    }
}
