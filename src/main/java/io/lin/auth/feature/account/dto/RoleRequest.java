package io.lin.auth.feature.account.dto;

import io.lin.auth.feature.account.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(
        @NotNull(message = "valid.role")
        Role role
) {
}
