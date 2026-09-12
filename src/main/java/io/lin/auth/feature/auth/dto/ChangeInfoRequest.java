package io.lin.auth.feature.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeInfoRequest(
        @NotBlank(message = "valid.username")
        String username,

        @NotBlank(message = "valid.email")
        @Email(message = "valid.email_format")
        String email,

        String displayName,

        String phone
) {
}
