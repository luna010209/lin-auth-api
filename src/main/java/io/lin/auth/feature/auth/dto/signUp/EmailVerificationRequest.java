package io.lin.auth.feature.auth.dto.signUp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(
        @NotBlank(message = "valid.email")
        @Email(message = "valid.email_format")
        String email,

        @NotBlank(message = "valid.verified_code")
        String verifiedCode
) {
}
