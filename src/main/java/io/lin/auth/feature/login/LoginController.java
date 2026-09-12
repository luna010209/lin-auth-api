package io.lin.auth.feature.login;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.login.dto.LoginRequest;
import io.lin.auth.feature.login.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "01. Authentication")
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "After authentication, JWT Access Token and Refresh Token are issued."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = loginService.authenticate(loginRequest);
        response.setHeader(AUTHORIZATION, "Bearer " + loginResponse.accessToken());
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @PostMapping("/find-username")
    @Operation(
            summary = "Find username (ID)",
            description = """
                    Retrieves the username associated with the given email address and sends it via email.
                    
                    Process flow:
                    - Check if an account exists with the provided email
                    - If it exists, trigger UsernameListenerEvent to send an email containing the username
                    
                    Failure conditions:
                    - No account is registered with the given email → Email has not been registered
                    
                    Security note:
                    - In production environments, consider returning a consistent response regardless of whether the account exists,
                      to prevent user enumeration attacks.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> findUsername(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        loginService.findUsername(email);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }

    @PostMapping("/find-password")
    @Operation(
            summary = "Reset password (issue temporary password)",
            description = """
                    Verifies the account using the provided email and username, then generates a temporary password and sends it via email.
                    The generated temporary password is immediately encoded and stored in the database, and the previous password becomes invalid.
                    
                    Process flow:
                    - Retrieve user by email (check if the account exists)
                    - (Recommended) Validate that the username also matches  ※ currently missing in the implementation
                    - Generate a temporary password (random 9 characters: letters, numbers, special characters)
                    - Encode and store the temporary password (auth.setPassword(encode))
                    - Trigger PasswordListenerEvent to send an email with the temporary password
                    
                    Failure conditions:
                    - No account is registered with the given email → Email has not been registered
                    
                    Security note:
                    - Instead of sending a temporary password in plain text via email,
                      using a password reset link (token-based approach) is more secure.
                    """
    )
    public ResponseEntity<ApiResponse<Void>> findPassword(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        loginService.findPassword(email);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }
}
