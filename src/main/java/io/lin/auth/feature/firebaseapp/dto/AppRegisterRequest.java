package io.lin.auth.feature.firebaseapp.dto;

import jakarta.validation.constraints.NotBlank;

public record AppRegisterRequest(
        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.password")
        String password,

        @NotBlank(message = "valid.confirm_password")
        String cfPassword,

        String displayName,

        String phone
) {
}
