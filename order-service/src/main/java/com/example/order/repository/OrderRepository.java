package com.example.order.repository;

import com.example.order.model.Order;
import com.example.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndCustomerCustomerId(Long id, String customerId);

    Page<Order> findAllByCustomerCustomerId(String customerId, Pageable pageable);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
