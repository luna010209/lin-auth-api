package io.lin.auth.feature.auth.dto.app;

import jakarta.validation.constraints.NotBlank;

public record AppConfirmRequest(
        @NotBlank(message = "valid.firebase")
        String uid,

        @NotBlank(message = "valid.email")
        String email,

        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.password")
        String password
) {
}
