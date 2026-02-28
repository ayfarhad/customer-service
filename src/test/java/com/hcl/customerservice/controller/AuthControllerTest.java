package com.hcl.customerservice.controller;

import com.hcl.customerservice.dto.AuthenticationRequest;
import com.hcl.customerservice.dto.AuthenticationResponse;
import com.hcl.customerservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void login_success() throws Exception {
        AuthenticationRequest req = new AuthenticationRequest("admin","admin123");
        UserDetails user = User.withUsername("admin").password("pwd").roles("ADMIN").build();
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("token");

        String json = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials() throws Exception {
        doThrow(new BadCredentialsException("bad")).when(authManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        String json = "{\"username\":\"bad\",\"password\":\"bad\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
