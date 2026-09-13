package io.lin.auth.feature.admin.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserIdsRequest(
        @NotEmpty List<Long> ids
) {
}
