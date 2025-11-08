package com.example.order.mapper;

import com.example.order.dto.request.CreateOrderOrderItemRequest;
import com.example.order.model.OrderItem;
import event.OrderItemEvent;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {
    OrderItem fromCreateOrderRequest(CreateOrderOrderItemRequest request);
    OrderItemEvent toEvent(OrderItem orderItem);
}
