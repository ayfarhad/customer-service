package com.hcl.customerservice.controller;

import com.hcl.customerservice.dto.CustomerRequest;
import com.hcl.customerservice.dto.CustomerResponse;
import com.hcl.customerservice.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class CustomerControllerTest {

    @Mock
    private CustomerService service;

    @InjectMocks
    private CustomerController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCustomer_callsService() throws Exception {
        CustomerResponse resp = CustomerResponse.builder().id(1L).name("A").email("a@b.com").build();
        when(service.getCustomerById(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).getCustomerById(1L);
    }

    @Test
    void getAllCustomers_callsService() throws Exception {
        Page<CustomerResponse> page = new PageImpl<>(Collections.singletonList(
                CustomerResponse.builder().id(1L).name("A").email("a@b.com").build()));
        when(service.getAllCustomers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(service).getAllCustomers(any(Pageable.class));
    }

    @Test
    void createCustomer_callsService() throws Exception {
        CustomerRequest req = CustomerRequest.builder().name("A").email("a@b.com").build();
        CustomerResponse resp = CustomerResponse.builder().id(1L).name("A").email("a@b.com").build();
        when(service.createCustomer(any(CustomerRequest.class))).thenReturn(resp);
        String json = "{\"name\":\"A\",\"email\":\"a@b.com\"}";

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(service).createCustomer(any(CustomerRequest.class));
    }
}
