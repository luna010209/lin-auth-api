package io.lin.auth.feature.login.jwt.cJwtConfig;

import io.lin.auth.feature.login.jwt.bToken.TokenProvider;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.login.jwt.TokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String jwt = TokenUtil.resolveToken(request);
        String requestURI = request.getRequestURI();

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (TokenUtil.isFirebaseToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (StringUtils.hasText(jwt)) {
            try {
                if (tokenProvider.validateToken(jwt)) {
                    Authentication authentication = tokenProvider.getAuthentication(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Authentication saved in Security Context for user: {}, uri: {}",
                            authentication.getName(), requestURI);
                }
            } catch (CustomException e) {

                if (Objects.equals("error.token.expired", e.getMessageCode())) {
                    response.setHeader(TokenUtil.TOKEN_EXPIRED_HEADER, "true");
                    logger.warn("JWT expired. uri: {}", requestURI);
                } else {
                    logger.warn("Invalid JWT. uri: {}", requestURI);
                }

            } catch (Exception e) {
                logger.error("Unexpected JWT processing error. uri: {}", requestURI, e);
            }
        } else {
            logger.debug("No JWT provided. uri: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }
}
