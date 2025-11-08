package com.example.order.mapper;

import com.example.order.dto.request.CreateOrderAddressRequest;
import com.example.order.model.embedded.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {
    Address fromCreateOrderRequest(CreateOrderAddressRequest request);
}
