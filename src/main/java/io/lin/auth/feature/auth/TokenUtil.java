package io.lin.auth.feature.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class TokenUtil {
    private TokenUtil(){}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_EXPIRED_HEADER = "X-Token-Expired";

    public static String resolveToken (HttpServletRequest request){
        String bearerToken = request.getHeader(TokenUtil.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer "))
            return bearerToken.substring(7);

        return null;
    }

    public static boolean isFirebaseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            return payloadJson.contains("\"iss\":\"https://securetoken.google.com/");
        } catch (Exception e) {
            return false;
        }
    }
}
