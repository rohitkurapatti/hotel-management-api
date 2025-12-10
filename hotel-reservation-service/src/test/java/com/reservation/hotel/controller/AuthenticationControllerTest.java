package com.reservation.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.hotel.model.AuthenticationRequest;
import com.reservation.hotel.model.AuthenticationResponse;
import com.reservation.hotel.commons.exception.GlobalExceptionHandler;
import com.reservation.hotel.model.RegisterRequest;
import com.reservation.hotel.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup MDC for traceId
        MDC.put("traceId", "test-trace-id-123");
    }

    @Test
    void registerWithValidRequestShouldReturnCreatedAndToken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe")
                .password("password123")
                .email("john@example.com")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token")
                .tokenType("Bearer")
                .username("johndoe")
                .role("USER")
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void authenticateWithValidCredentialsShouldReturnOkAndToken() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("johndoe")
                .password("password123")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.login.token")
                .tokenType("Bearer")
                .username("johndoe")
                .role("ADMIN")
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.login.token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void registerWithInvalidRequestShouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequestNoUsername = RegisterRequest.builder()
                .username("")  // blank username
                .password("password123")
                .email("john@example.com")
                .build();

        RegisterRequest invalidRequestNoPassword = RegisterRequest.builder()
                .username("johndoe")
                .password("")  // blank password
                .email("john@example.com")
                .build();

        RegisterRequest invalidRequestInvalidEmail = RegisterRequest.builder()
                .username("johndoe")
                .password("password123")
                .email("invalid-email")  // invalid email format
                .build();

        RegisterRequest invalidRequestShortPassword = RegisterRequest.builder()
                .username("johndoe")
                .password("12345")  // less than 6 characters
                .email("john@example.com")
                .build();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestNoUsername)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestNoPassword)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestInvalidEmail)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestShortPassword)))
                .andExpect(status().isBadRequest());

        // Verify service was never called due to validation failure
        verify(authenticationService, never()).register(any(RegisterRequest.class));
    }
}

