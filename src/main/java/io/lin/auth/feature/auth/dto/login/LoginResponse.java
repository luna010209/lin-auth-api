package io.lin.auth.feature.auth.dto.login;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
