package io.lin.auth.feature.auth.dto;

import io.lin.auth.feature.auth.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleRequest(
        @NotNull(message = "valid.role")
        Role role
) {
}
