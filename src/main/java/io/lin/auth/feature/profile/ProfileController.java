package io.lin.auth.feature.profile;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.account.dto.ChangeInfoRequest;
import io.lin.auth.feature.account.dto.ChangePasswordRequest;
import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.login.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "01. Authentication")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(
            summary = "Confirm login user information",
            description = "Get user information"
    )
    public ResponseEntity<ApiResponse<UserInfo>> currentUser(@CurrentUser Auth user) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(user)));
    }

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
    public ResponseEntity<ApiResponse<Void>> changeInfo(
            @CurrentUser Auth user,
            @Valid @RequestBody ChangeInfoRequest request
    ) {
        profileService.changeInfo(user, request);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }

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
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @CurrentUser Auth user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(user, request);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }

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
    public ResponseEntity<ApiResponse<Void>> changeAvatar(
            @CurrentUser Auth user,
            @RequestParam("avatar") MultipartFile avatar
    ) {
        profileService.changeAvatar(user, avatar);
        return ResponseEntity.ok(ApiResponse.okVoid());
    }
}
