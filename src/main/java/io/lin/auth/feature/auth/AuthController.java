package io.lin.auth.feature.auth;

import io.lin.auth.feature.auth.dto.ChangeInfoRequest;
import io.lin.auth.feature.auth.dto.ChangePasswordRequest;
import io.lin.auth.feature.auth.dto.UserInfo;
import io.lin.auth.feature.auth.dto.login.LoginRequest;
import io.lin.auth.feature.auth.dto.login.LoginResponse;
import io.lin.auth.feature.auth.dto.signUp.EmailVerificationRequest;
import io.lin.auth.feature.auth.dto.signUp.SignUpRequest;
import io.lin.auth.feature.auth.service.AuthService;
import io.lin.auth.feature.auth.service.login.LoginService;
import io.lin.auth.feature.auth.service.signUp.SignUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "01. Authentication")
public class AuthController {

    private final SignUpService signUpService;
    private final LoginService loginService;
    private final AuthService authService;

    /*
    -------------------------------------------------------------------
        PUBLIC API
    -------------------------------------------------------------------
     */

    // Email verification
    @PostMapping("/email-verification/send-mail")
    @Operation(
            summary = "Send verification email",
            description = """
                    Send verification code to your email to sign up.
                    This verified code is valid in 15 minutes.
                    """
    )
    public ResponseEntity<Void> sendMail(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        signUpService.sendMail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-verification/verify")
    @Operation(
            summary = "Verify code",
            description = """
                    Use the code you received to confirm email.
                    Then this email can be used to sign up later.
                    """
    )
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        signUpService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }


    // Sign Up
    @PostMapping("/sign-up")
    @Operation(
            summary = "Sign up",
            description = """
                    Sign up with below information:
                    - username, password, verified email, phone, display name
                    """
    )
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        signUpService.signUp(request);
        return ResponseEntity.ok().build();
    }


    // Login
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "After authentication, JWT Access Token and Refresh Token are issued."
    )
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = loginService.authenticate(loginRequest);
        response.setHeader(AUTHORIZATION, "Bearer " + loginResponse.accessToken());
        return ResponseEntity.ok(loginResponse);
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
    public ResponseEntity<String> findUsername(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        loginService.findUsername(email);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<String> findPassword(
            @NotBlank(message = "valid.email")
            @Email(message = "valid.email_format")
            @RequestParam("email")
            String email
    ) {
        loginService.findPassword(email);
        return ResponseEntity.ok().build();
    }


    /*
    -------------------------------------------------------------------
        PRIVATE API
    -------------------------------------------------------------------
     */

    // Get user profile
    @GetMapping
    @Operation(
            summary = "Confirm login user information",
            description = "Get user information"
    )
    public UserInfo currentUser() {
        return authService.userLogin();
    }


    //Change user basic information
    @PutMapping
    @Operation(
            summary = "Update user basic information",
            description = """
                    Updates the basic information of the logged-in user.
                        - username (must be unique)
                        - dislay name
                        - phone
                        - email (new email should be verified first)
                    """
    )
    public ResponseEntity<Void> changeInfo(
            @Valid @RequestBody ChangeInfoRequest request
    ) {
        authService.changeInfo(request);
        return ResponseEntity.ok().build();
    }


    //Change password
    @PutMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = """
                    Changes the password of the currently logged-in user.
                    
                    Process:
                    - Retrieve the currently logged-in user from SecurityContext (login required)
                    - Verify that currentPassword matches the existing password
                    - Check that newPassword and cfPassword (confirmation password) match
                    - If validation passes, encrypt the newPassword (e.g., using BCrypt) and save it
                    """
    )
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }


    //Change avatar
    @PutMapping(value = "/avatar", consumes = "multipart/form-data")
    @Operation(
            summary = "Change avatar (profile image)",
            description = """
                    Uploads and updates the profile image of the logged-in user.
                    - The image file must be sent as multipart/form-data.
                    - Supported formats: image/jpeg, image/png, image/webp
                    - The existing image will be overwritten at the same path.
                    """
    )
    public ResponseEntity<Void> changeAvatar(
            @RequestParam("avatar") MultipartFile avatar
    ) {
        authService.changeAvatar(avatar);
        return ResponseEntity.ok().build();
    }

}
