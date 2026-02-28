package com.hcl.customerservice.mapper;

import com.hcl.customerservice.dto.CustomerRequest;
import com.hcl.customerservice.dto.CustomerResponse;
import com.hcl.customerservice.entity.Customer;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }
        return Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
    }

    public static CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .build();
    }
}
