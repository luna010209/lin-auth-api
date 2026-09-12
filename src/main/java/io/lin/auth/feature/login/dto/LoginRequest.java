package io.lin.auth.feature.login.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.password")
        String password
) {
}
