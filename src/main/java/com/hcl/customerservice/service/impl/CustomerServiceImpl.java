package com.hcl.customerservice.service.impl;

import com.hcl.customerservice.dto.CustomerRequest;
import com.hcl.customerservice.dto<CustomerResponse;
import com.hcl.customerservice.entity.Customer;
import com.hcl.customerservice.exception.CustomerNotFoundException;
import com.hcl.customerservice.exception.EmailAlreadyExistsException;
import com.hcl.customerservice.mapper.CustomerMapper;
import com.hcl.customerservice.repository.CustomerRepository;
import com.hcl.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository repository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        Customer saved = repository.save(CustomerMapper.toEntity(request));
        logger.info("Created customer {}", saved.getId());
        return CustomerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return repository.findAll(pageable).map(CustomerMapper::toResponse);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        if (!customer.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        Customer updated = repository.save(customer);
        logger.info("Updated customer {}", id);
        return CustomerMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!repository.existsById(id)) {
            throw new CustomerNotFoundException(id);
        }
        repository.deleteById(id);
        logger.info("Deleted customer {}", id);
    }
}
