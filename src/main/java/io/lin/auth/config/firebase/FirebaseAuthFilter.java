package io.lin.auth.config.firebase;

import io.lin.auth.feature.auth.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String idToken = TokenUtil.resolveToken(request);

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (idToken == null || !TokenUtil.isFirebaseToken(idToken)){
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication = firebaseTokenVerifier.getAuth(idToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setHeader(TokenUtil.TOKEN_EXPIRED_HEADER, "true");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
