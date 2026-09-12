package io.lin.auth.feature.login.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
