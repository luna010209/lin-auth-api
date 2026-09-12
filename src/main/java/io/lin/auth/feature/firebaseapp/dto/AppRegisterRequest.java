package io.lin.auth.feature.firebaseapp.dto;

import jakarta.validation.constraints.NotBlank;

public record AppRegisterRequest(
        @NotBlank(message = "valid.firebase")
        String uid,

        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.password")
        String password,

        @NotBlank(message = "valid.confirm_password")
        String cfPassword,

        @NotBlank(message = "valid.email")
        String email,

        String displayName,

        String phone
) {
}
