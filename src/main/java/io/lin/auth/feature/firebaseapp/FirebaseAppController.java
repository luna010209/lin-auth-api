package io.lin.auth.feature.firebaseapp;

import com.google.common.net.HttpHeaders;
import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.config.firebase.FirebaseTokenVerifier;
import io.lin.auth.feature.firebaseapp.dto.AppLoginResponse;
import io.lin.auth.feature.firebaseapp.dto.AppRegisterRequest;
import io.lin.auth.feature.firebaseapp.dto.VerifiedFirebaseIdentity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/app")
@Tag(name = "01. Authentication")
public class FirebaseAppController {

    private final FirebaseAppService firebaseAppService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @PostMapping
    @Operation(
            summary = "Check app login status after Firebase Google sign-in",
            description = """
                        Call after the client completes Google sign-in with Firebase Auth.
                    
                        Headers:
                        - Authorization: Bearer {firebaseIdToken}
                    
                        Process:
                        1) Verify Firebase ID token server-side (uid + email come from token)
                        2) If firebase_uid exists in DB → LOGIN_SUCCESS
                        3) Else if email exists → auto-link firebase_uid and LOGIN_SUCCESS
                        4) Else → NEED_REGISTER with profile (email, displayName, uid, picture) for the registration form
                    """
    )
    public ResponseEntity<ApiResponse<AppLoginResponse>> appLogin(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    ) {
        VerifiedFirebaseIdentity identity = firebaseTokenVerifier.verifyIdentity(authorization);
        return ResponseEntity.ok(ApiResponse.ok(firebaseAppService.appLogin(identity)));
    }

    @PostMapping("register")
    @Operation(
            summary = "Register a new account from Firebase Google sign-in",
            description = """
                        Used when POST /auth/app returns NEED_REGISTER.
                    
                        Headers:
                        - Authorization: Bearer {firebaseIdToken}
                    
                        Body:
                        - username, password, cfPassword
                        - displayName, phone (optional; displayName falls back to Google name)
                    
                        Email and firebase_uid come from the verified ID token.
                        Google email_verified=true is trusted — no email_verification table required.
                    """
    )
    public ResponseEntity<ApiResponse<AppLoginResponse>> register(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @Valid @RequestBody AppRegisterRequest request
    ) {
        VerifiedFirebaseIdentity identity = firebaseTokenVerifier.verifyIdentity(authorization);
        return ResponseEntity.ok(ApiResponse.ok(firebaseAppService.newAccount(identity, request)));
    }
}
