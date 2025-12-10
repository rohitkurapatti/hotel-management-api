package com.reservation.hotel.security;

import com.reservation.hotel.security.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, userDetailsService);
    }

    @Test
    void passwordEncoderShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder);
        assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder",
                     passwordEncoder.getClass().getName());

        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void authenticationProviderShouldReturnDaoAuthenticationProvider() {
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();
        assertNotNull(authProvider);
        assertInstanceOf(DaoAuthenticationProvider.class, authProvider);
        DaoAuthenticationProvider daoAuthProvider = (DaoAuthenticationProvider) authProvider;
        assertNotNull(daoAuthProvider);
    }

    @Test
    void authenticationManagerShouldReturnAuthenticationManagerFromConfiguration() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);
        assertNotNull(result);
        assertEquals(authenticationManager, result);
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    void securityFilterChainShouldBeConfiguredCorrectly() throws Exception {
        // Given
        org.springframework.security.config.annotation.web.builders.HttpSecurity http =
            mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class, RETURNS_SELF);

        SecurityFilterChain mockFilterChain = mock(SecurityFilterChain.class);
        doReturn(mockFilterChain).when(http).build();
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);

        assertNotNull(filterChain);
        assertEquals(mockFilterChain, filterChain);

        verify(http, times(1)).csrf(any());
        verify(http, times(1)).authorizeHttpRequests(any());
        verify(http, times(1)).sessionManagement(any());
        verify(http, times(1)).authenticationProvider(any());
        verify(http, times(1)).addFilterBefore(any(), any());
        verify(http, times(1)).build();
    }
}

