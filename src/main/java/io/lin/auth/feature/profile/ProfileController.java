package io.lin.auth.feature.profile;

import io.lin.auth.feature.account.dto.ChangeInfoRequest;
import io.lin.auth.feature.account.dto.ChangePasswordRequest;
import io.lin.auth.feature.account.dto.UserInfo;
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
    public UserInfo currentUser() {
        return profileService.userLogin();
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
    public ResponseEntity<Void> changeInfo(
            @Valid @RequestBody ChangeInfoRequest request
    ) {
        profileService.changeInfo(request);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(request);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> changeAvatar(
            @RequestParam("avatar") MultipartFile avatar
    ) {
        profileService.changeAvatar(avatar);
        return ResponseEntity.ok().build();
    }
}
