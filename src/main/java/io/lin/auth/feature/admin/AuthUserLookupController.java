package io.lin.auth.feature.admin;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.admin.dto.AuthSummary;
import io.lin.auth.feature.admin.dto.UserIdsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/users")
@Tag(name = "Auth users")
public class AuthUserLookupController {

    private final AdminUserService adminUserService;

    @PostMapping("/lookup")
    @Operation(summary = "Resolve user display names by auth id")
    public ResponseEntity<ApiResponse<List<AuthSummary>>> lookupUsers(
            @Valid @RequestBody UserIdsRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.lookupUsers(request.ids())));
    }

    @GetMapping("/{userId}/exists")
    @Operation(summary = "Check whether an auth user id exists")
    public ResponseEntity<ApiResponse<Void>> userExists(@PathVariable Long userId) {
        adminUserService.requireUserExists(userId);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }
}
