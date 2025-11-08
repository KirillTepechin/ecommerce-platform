package com.example.payment.service;

import com.example.payment.model.Payment;
import com.example.payment.model.enums.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment createPayment(Long orderId, String customerId, BigDecimal amount) {
        log.info("Creating payment for order: {}, customer: {}, amount: {}", orderId, customerId, amount);

        // Проверяем, не существует ли уже платеж для этого заказа
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("Payment already exists for order: " + orderId);
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .customerId(customerId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for order: " + orderId));
    }

    public Payment completePayment(Long paymentId, String transactionId) {
        log.info("Completing payment: {} with transaction: {}", paymentId, transactionId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        payment.processSuccess(transactionId);
        return paymentRepository.save(payment);
    }

    public Payment failPayment(Long paymentId, String failureReason) {
        log.info("Failing payment: {} with reason: {}", paymentId, failureReason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        payment.processFailure(failureReason);
        return paymentRepository.save(payment);
    }

    public List<Payment> getCustomerPayments(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    public boolean existsByOrderId(Long orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }
}