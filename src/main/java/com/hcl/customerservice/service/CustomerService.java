package com.hcl.customerservice.service;

import com.hcl.customerservice.dto.CustomerRequest;
import com.hcl.customerservice.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse getCustomerById(Long id);
    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
}
