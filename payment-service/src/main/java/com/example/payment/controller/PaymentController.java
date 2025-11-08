package com.example.payment.controller;

import com.example.payment.dto.PaymentDto;
import com.example.payment.mapper.PaymentMapper;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDto> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("Fetching payment for order: {}", orderId);

        PaymentDto payment = paymentMapper.toDto(paymentService.getPaymentByOrderId(orderId));
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentDto>> getCustomerPayments(@PathVariable String customerId) {
        log.info("Fetching payments for customer: {}", customerId);

        List<PaymentDto> payments = paymentMapper.toDtoList(paymentService.getCustomerPayments(customerId));
        return ResponseEntity.ok(payments);
    }
}
