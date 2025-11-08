package com.example.payment.repository;

import com.example.payment.model.Payment;
import com.example.payment.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByCustomerId(String customerId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId AND p.status = :status")
    List<Payment> findByCustomerIdAndStatus(String customerId, PaymentStatus status);

    boolean existsByOrderId(Long orderId);
}
