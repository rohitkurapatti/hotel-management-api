package com.reservation.hotel.services;

import com.reservation.hotel.model.AuthenticationRequest;
import com.reservation.hotel.model.AuthenticationResponse;
import com.reservation.hotel.model.RegisterRequest;
import com.reservation.hotel.model.Role;
import com.reservation.hotel.entities.User;
import com.reservation.hotel.repository.UserRepository;
import com.reservation.hotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[TraceId: {}] Registering new user: {}", traceId, request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.error("[TraceId: {}] Username already exists: {}", traceId, request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("[TraceId: {}] Email already exists: {}", traceId, request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("[TraceId: {}] User registered successfully: {}", traceId, user.getUsername());

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[TraceId: {}] Authenticating user: {}", traceId, request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        var jwtToken = jwtService.generateToken(user);
        log.info("[TraceId: {}] User authenticated successfully: {}", traceId, user.getUsername());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}

