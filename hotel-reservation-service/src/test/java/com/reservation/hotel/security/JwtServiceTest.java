package com.reservation.hotel.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {JwtService.class})
@TestPropertySource(properties = {
        "jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "jwt.expiration=86400000"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateTokenWithUserDetailsShouldReturnValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void generateTokenWithExtraClaimsShouldIncludeClaimsInToken() {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("userId", 12345);

        String token = jwtService.generateToken(extraClaims, userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Integer userId = jwtService.extractClaim(token, claims -> claims.get("userId", Integer.class));
        assertEquals("ADMIN", role);
        assertEquals(12345, userId);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValidWithValidTokenAndCorrectUserShouldReturnTrue() {

        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("testuser", extractedUsername);

        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenValidWithWrongUsernameShouldReturnFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);
        assertFalse(isValid);
    }
}

