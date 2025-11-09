package com.example.order.service;

import com.example.order.dto.OrderDto;
import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.mapper.OrderMapper;
import com.example.order.model.Order;
import com.example.order.model.enums.OrderStatus;
import com.example.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    private final OrderRepository orderRepository;

    private final OrderEventPublisher orderEventPublisher;

    // Создание заказа клиентом
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomer().getCustomerEmail());

        // Создаем заказ
        final Order order = orderMapper.fromCreateRequest(request);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(
                order.getItems().stream()
                        .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Сохраняем заказ
        final Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Публикуем событие в Kafka
        try {
            //TODO: Проверить флоу
            orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        } catch (Exception e) {
            log.error("Failed to publish OrderCreated event for order: {}", savedOrder.getId(), e);
            // Можно добавить retry logic или сохранить в outbox table
        }

        return orderMapper.toDto(savedOrder);
    }

    // Получение заказа клиента
    public OrderDto getCustomerOrder(Long orderId, String customerId) {
        log.info("Fetching order ID: {} for customer: {}", orderId, customerId);

        final Order order = orderRepository.findByIdAndCustomerCustomerId(orderId, customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order not found with ID: %s for customer: %s", orderId, customerId)
                ));

        return orderMapper.toDto(order);
    }

    // Получение всех заказов клиента
    public List<OrderDto> getCustomerOrders(String customerId, Pageable pageable) {
        log.info("Fetching all orders for customer: {}", customerId);

        return orderRepository.findAllByCustomerCustomerId(customerId, pageable)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    // Отмена заказа клиентом
    @Transactional
    public OrderDto cancelOrderByCustomer(Long orderId, String customerId) {
        log.info("Customer cancelling order ID: {}", orderId);

        final Order order = orderRepository.findByIdAndCustomerCustomerId(orderId, customerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Order not found with ID: %s for customer: %s", orderId, customerId)
                ));

        // Клиент может отменять только заказы в статусах PENDING и CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    String.format("Cannot cancel order in status: %s", order.getStatus())
            );
        }

        cancelOrder(order);
        final Order cancelledOrder = orderRepository.save(order);

        // TODO: Отправить событие OrderCancelled в Kafka для inventory и payment сервисов

        log.info("Order ID: {} cancelled by customer", orderId);
        return orderMapper.toDto(cancelledOrder);
    }

    // Для админского API (будет позже)
    public List<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findAllByStatus(status, pageable)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    private void cancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }
    }
}
