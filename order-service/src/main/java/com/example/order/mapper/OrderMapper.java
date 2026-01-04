package com.example.order.mapper;

import com.example.order.dto.OrderDto;
import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.model.Order;
import event.OrderCreatedEvent;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {AddressMapper.class, OrderItemMapper.class})
public interface OrderMapper {
    Order fromCreateRequest(CreateOrderRequest request);

    OrderDto toDto(Order order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerId", source = "customer.customerId")
    OrderCreatedEvent toCreatedEvent(Order order);

    @AfterMapping
    default void afterMapping(@MappingTarget Order order) {
        order.getItems().forEach(orderItem -> orderItem.setOrder(order));
    }
}
