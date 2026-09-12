package io.lin.auth.feature.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        int expirationMinutes,
        int refreshExpirationMinutes,
        String secretKey
) {
}
