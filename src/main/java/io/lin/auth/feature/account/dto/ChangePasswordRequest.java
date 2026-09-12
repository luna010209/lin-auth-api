package io.lin.auth.feature.account.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String cfPassword
) {
}
