package com.example.payment.service;

import com.example.payment.model.Payment;
import event.OrderCreatedEvent;
import event.PaymentCompletedEvent;
import event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event for order: {}", event.getId());
        try {
            // Проверяем, не обрабатывали ли мы уже этот заказ
            if (paymentService.existsByOrderId(event.getId())) {
                log.warn("Payment already exists for order: {}, skipping", event.getId());
                return;
            }

            // Создаем платеж
            final Payment payment = paymentService.createPayment(
                    event.getId(),
                    event.getCustomerId(),
                    event.getTotalAmount()
            );

            // Обрабатываем платеж
            processPayment(payment);

        } catch (Exception e) {
            log.error("Error processing payment for order: {}", event.getId(), e);
            // TODO: Отправить в Dead Letter Topic
        }
    }

    private void processPayment(Payment payment) {
        try {
            log.info("Processing payment for order: {}, amount: {}", payment.getOrderId(), payment.getAmount());

            // Имитация обработки платежа (2-5 секунд)
            Thread.sleep(2000 + (long) (Math.random() * 3000));

            // 90% успешных платежей, 10% неудачных
            boolean isSuccessful = Math.random() > 0.1;

            if (isSuccessful) {
                // Успешный платеж
                String transactionId = "TXN_" + System.currentTimeMillis();
                Payment completedPayment = paymentService.completePayment(payment.getId(), transactionId);

                // Публикуем событие успешного платежа
                publishPaymentCompletedEvent(completedPayment);

                log.info("Payment completed for order: {}, transaction: {}",
                        payment.getOrderId(), transactionId);

            } else {
                // Неудачный платеж
                final String failureReason = "Insufficient funds";
                final Payment failedPayment = paymentService.failPayment(payment.getId(), failureReason);

                // Публикуем событие неудачного платежа
                publishPaymentFailedEvent(failedPayment);

                log.warn("Payment failed for order: {}, reason: {}",
                        payment.getOrderId(), failureReason);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted for order: {}", payment.getOrderId(), e);
        } catch (Exception e) {
            log.error("Unexpected error during payment processing for order: {}", payment.getOrderId(), e);
            Payment failedPayment = paymentService.failPayment(payment.getId(), "Internal server error");
            publishPaymentFailedEvent(failedPayment);
        }
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        final PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .transactionId(payment.getTransactionId())
                .completedAt(payment.getProcessedAt())
                .build();

        kafkaTemplate.send("payment-completed", event);
        log.debug("Published PaymentCompleted event for order: {}", payment.getOrderId());
    }

    private void publishPaymentFailedEvent(Payment payment) {
        final PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .failureReason(payment.getFailureReason())
                .failedAt(payment.getProcessedAt())
                .build();

        kafkaTemplate.send("payment-failed", event);
        log.debug("Published PaymentFailed event for order: {}", payment.getOrderId());
    }
}