package io.lin.auth.feature.auth.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String cfPassword
) {
}
