package io.lin.auth.feature.signup;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.signup.dto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "01. Authentication")
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Sign up",
            description = """
                    Sign up with below information:
                    - username, password, verified email, phone, display name
                    """
    )
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        signUpService.signUp(request);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }
}
