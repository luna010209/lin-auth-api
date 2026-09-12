package io.lin.auth.feature.auth.dto;

import io.lin.auth.feature.auth.entity.Auth;
import io.lin.auth.feature.auth.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record UserInfo(
        Long id,
        String username,
        String displayName,
        String email,
        String phone,
        String uid,
        String avatar,
        LocalDateTime lastLogin,
        LocalDateTime createdAt,
        List<String> roles
) {
    public static UserInfo fromEntity(Auth user, String avatar) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhone(),
                user.getUid(),
                avatar,
                user.getLastLogin(),
                user.getCreatedAt(),
                user.getRoles().stream().map(Role::getAuthority).toList()
        );
    }
}
