package io.lin.auth.feature.emailverification;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.emailverification.dto.EmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/email-verification")
@Tag(name = "01. Authentication")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-mail")
    @Operation(
            summary = "Send verification email",
            description = """
                    Send verification code to your email to sign up.
                    This verified code is valid in 15 minutes.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> sendMail(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        emailVerificationService.sendMail(email);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }

    @PostMapping("/verify")
    @Operation(
            summary = "Verify code",
            description = """
                    Use the code you received to confirm email.
                    Then this email can be used to sign up later.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }
}
