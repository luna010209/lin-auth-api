package io.lin.auth.feature.firebaseapp.dto;

import jakarta.validation.constraints.NotBlank;

public record AppLoginRequest(
        @NotBlank(message = "valid.firebase")
        String uid,

        String email
) {
}
