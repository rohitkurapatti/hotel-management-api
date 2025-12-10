package com.reservation.hotel.services;

import com.reservation.hotel.dto.AuthenticationRequest;
import com.reservation.hotel.dto.AuthenticationResponse;
import com.reservation.hotel.dto.RegisterRequest;
import com.reservation.hotel.model.Role;
import com.reservation.hotel.model.User;
import com.reservation.hotel.repository.UserRepository;
import com.reservation.hotel.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;

    @BeforeEach
    void setUp() {
        MDC.put("traceId", "test-trace-id");

        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void registerWithValidRequestShouldRegisterUserAndReturnToken() {
        String encodedPassword = "encodedPassword123";
        String jwtToken = "jwt.token.here";

        setupSuccessfulRegistrationMocks(encodedPassword, jwtToken);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertAuthenticationResponse(response, jwtToken, "testuser", "USER");
        verifySavedUser(encodedPassword);
        verifyRegistrationInteractions();
    }

    @Test
    void registerWithExistingUsernameOrEmailShouldThrowIllegalArgumentException() {
        // Test Case 1: Existing username
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertRegistrationException("Username already exists");
        verify(userRepository, never()).save(any(User.class));

        reset(userRepository);

        // Test Case 2: Existing email
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertRegistrationException("Email already exists");
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateWithValidCredentialsShouldReturnToken() {
        String jwtToken = "jwt.token.here";

        setupSuccessfulAuthenticationMocks(jwtToken);

        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        assertAuthenticationResponse(response, jwtToken, "testuser", "USER");
        verifyAuthenticationToken();
        verify(userRepository, times(1)).findByUsername(authenticationRequest.getUsername());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void authenticateWithInvalidCredentialsShouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authenticationService.authenticate(authenticationRequest));

        assertEquals("Bad credentials", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(any());
        verify(jwtService, never()).generateToken(any());
    }

    // Helper methods to reduce code duplication

    private void setupSuccessfulRegistrationMocks(String encodedPassword, String jwtToken) {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);
    }

    private void setupSuccessfulAuthenticationMocks(String jwtToken) {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername(authenticationRequest.getUsername()))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);
    }

    private void assertAuthenticationResponse(AuthenticationResponse response, String expectedToken, String expectedUsername, String expectedRole) {
        assertNotNull(response);
        assertEquals(expectedToken, response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(expectedUsername, response.getUsername());
        assertEquals(expectedRole, response.getRole());
    }

    private void assertRegistrationException(String expectedMessage) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.register(registerRequest));
        assertEquals(expectedMessage, exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
    }

    private void verifySavedUser(String expectedEncodedPassword) {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(registerRequest.getUsername(), savedUser.getUsername());
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(expectedEncodedPassword, savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertTrue(savedUser.isEnabled());
    }

    private void verifyRegistrationInteractions() {
        verify(userRepository, times(1)).existsByUsername(registerRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    private void verifyAuthenticationToken() {
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager, times(1)).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken capturedAuth = authCaptor.getValue();
        assertEquals(authenticationRequest.getUsername(), capturedAuth.getPrincipal());
        assertEquals(authenticationRequest.getPassword(), capturedAuth.getCredentials());
    }
}

