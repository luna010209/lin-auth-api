package io.lin.auth.feature.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.password")
        String password
) {
}
