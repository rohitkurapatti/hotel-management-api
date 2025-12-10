package com.reservation.hotel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for user authentication")
public class AuthenticationRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

