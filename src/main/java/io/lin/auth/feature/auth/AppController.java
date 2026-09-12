package io.lin.auth.feature.auth;

import io.lin.auth.feature.auth.dto.app.AppConfirmRequest;
import io.lin.auth.feature.auth.dto.app.AppLoginRequest;
import io.lin.auth.feature.auth.dto.app.AppLoginResponse;
import io.lin.auth.feature.auth.dto.app.AppRegisterRequest;
import io.lin.auth.feature.auth.service.app.AppService;
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
@RequestMapping("/auth/app")
@Tag(name = "01. Authentication")
public class AppController {
    private final AppService appService;

    @PostMapping
    @Operation(
            summary = "Check app login status (based on Firebase UID)",
            description = """
                        Determines the login status based on the server database after Firebase login (e.g., Google) is completed in the Flutter app.
                    
                        Process:
                        1) Retrieve user by firebaseUid
                           - If exists: LOGIN_SUCCESS (login immediately)
                        2) If firebaseUid does not exist, check existing account by email
                           - If exists: EMAIL_EXIST → proceed to account linking confirmation step
                        3) If email also does not exist
                           - NEED_REGISTER → redirect to the registration screen
                    
                        Note:
                        - This API does NOT perform password verification. (Account linking is handled in /confirm)
                        - It is recommended to replace client-provided uid/email with Firebase ID Token verification in the future.
                    """
    )
    public ResponseEntity<AppLoginResponse> appLogin(
            @Valid @RequestBody AppLoginRequest request
    ) {
        return ResponseEntity.ok(appService.appLogin(request));
    }


    @PostMapping("confirm")
    @Operation(
            summary = "Verify existing web account and link Firebase UID",
            description = """
                        After Firebase login, links an existing web account (with the same email or phone number)
                        to the Firebase UID after verifying the user's identity (username/password).
                    
                        Usage scenario:
                        - Called when /app-login returns EMAIL_EXIST (or PHONE_EXIST)
                    
                        Process:
                        1) Verify the existing account using username/password
                        2) If verification succeeds, update the account’s firebaseUid with the provided UID
                        3) After linking, the user can log in directly using firebaseUid
                    """
    )
    public ResponseEntity<AppLoginResponse> confirmUser(
            @Valid @RequestBody AppConfirmRequest request
    ) {
        return ResponseEntity.ok(appService.confirmUser(request));
    }


    @PostMapping("register")
    @Operation(
            summary = "App registration (create account based on Firebase UID)",
            description = """
                        Registers a new user when no account exists in the database after Firebase login.
                        The Firebase UID is stored during registration, allowing simplified login via UID in the app.
                    
                        Usage scenario:
                        - Called when /app-login returns NEED_REGISTER
                    
                        Process:
                        1) Validate username (uniqueness and format)
                        2) Verify that password and confirmPassword match
                        3) Check for duplicate email (or phone)
                        4) Create a new account and store the firebaseUid
                    
                        Note:
                        - Depending on policy, you may either reuse the web email verification flow (require verification),
                          or treat emails from Google login as already verified.
                    """
    )
    public ResponseEntity<AppLoginResponse> register(
            @Valid @RequestBody AppRegisterRequest request
    ) {
        return ResponseEntity.ok(appService.newAccount(request));
    }


    @PostMapping("verify-email")
    @Operation(
            summary = "Verify email for social login registration",
            description = """
                        Marks the email received after a successful social login (e.g., Google) as verified for registration.
                    
                        Process:
                        - Normalize the email by trimming and converting it to lowercase
                        - Store the email as verified (verified=true)
                    
                        Usage scenario:
                        - User successfully logs in via Google or other social providers in the app
                        - The email is reliably provided by the social account
                        - Allows registration to proceed without entering an email verification code
                    
                        Note:
                        - This API is intended for use when a trusted email is obtained from social login.
                        - It can replace the standard email verification code process for regular email registration.
                    """
    )
    public ResponseEntity<Void> verifyEmail(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email") String email
    ) {
        appService.verifyEmail(email);
        return ResponseEntity.ok().build();
    }
}
