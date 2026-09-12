package io.lin.auth.feature.auth.dto.app;

import jakarta.validation.constraints.NotBlank;

public record AppLoginRequest(
        @NotBlank(message = "valid.firebase")
        String uid,

        String email
) {
}
