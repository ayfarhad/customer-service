package com.hcl.customerservice.service.impl;

import com.hcl.customerservice.dto.CustomerRequest;
import com.hcl.customerservice.dto.CustomerResponse;
import com.hcl.customerservice.entity.Customer;
import com.hcl.customerservice.exception.CustomerNotFoundException;
import com.hcl.customerservice.exception.EmailAlreadyExistsException;
import com.hcl.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCustomer_success() {
        CustomerRequest req = CustomerRequest.builder().name("Alice").email("alice@example.com").build();
        when(repository.existsByEmail(req.getEmail())).thenReturn(false);
        Customer saved = Customer.builder().id(1L).name(req.getName()).email(req.getEmail()).build();
        when(repository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse resp = service.createCustomer(req);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Alice", resp.getName());
    }

    @Test
    void createCustomer_emailExists() {
        CustomerRequest req = CustomerRequest.builder().name("Alice").email("alice@example.com").build();
        when(repository.existsByEmail(req.getEmail())).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> service.createCustomer(req));
    }

    @Test
    void getCustomerById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class, () -> service.getCustomerById(1L));
    }

    @Test
    void getAllCustomers_empty() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<CustomerResponse> page = service.getAllCustomers(Pageable.unpaged());
        assertNotNull(page);
        assertTrue(page.isEmpty());
    }

    @Test
    void updateCustomer_success() {
        Customer existing = Customer.builder().id(1L).name("Old").email("old@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerRequest req = CustomerRequest.builder().name("New").email("new@example.com").build();
        CustomerResponse resp = service.updateCustomer(1L, req);

        assertEquals("New", resp.getName());
        assertEquals("new@example.com", resp.getEmail());
    }

    @Test
    void updateCustomer_conflict() {
        Customer existing = Customer.builder().id(1L).name("Old").email("old@example.com").build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("taken@example.com")).thenReturn(true);
        CustomerRequest req = CustomerRequest.builder().name("New").email("taken@example.com").build();
        assertThrows(EmailAlreadyExistsException.class, () -> service.updateCustomer(1L, req));
    }

    @Test
    void deleteCustomer_notFound() {
        when(repository.existsById(1L)).thenReturn(false);
        assertThrows(CustomerNotFoundException.class, () -> service.deleteCustomer(1L));
    }

    @Test
    void deleteCustomer_success() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);
        assertDoesNotThrow(() -> service.deleteCustomer(1L));
        verify(repository).deleteById(1L);
    }
}
