package io.lin.auth.feature.signup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.display_name")
        String displayName,

        @NotBlank(message = "valid.password")
        String password,

        @NotBlank(message = "valid.confirm_password")
        String cfPassword,

        @NotBlank(message = "valid.email")
        @Email(message = "valid.email_format")
        String email,

        String phone
) {
}
