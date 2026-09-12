package io.lin.auth.feature.auth.jwt.bToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import io.lin.auth.feature.auth.jwt.JwtProperties;
import io.lin.auth.feature.auth.jwt.aUserDetails.CustomUserDetails;
import io.lin.auth.feature.auth.jwt.aUserDetails.CustomUserDetailsService;
import io.lin.auth.feature.auth.entity.Auth;
import io.lin.auth.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

    private static final Logger staticLog = LoggerFactory.getLogger(TokenProvider.class);

    private final SecretKey signingKey;
    private final CustomUserDetailsService userDetailsService;

    public TokenProvider(JwtProperties properties, CustomUserDetailsService userDetailsService){
        this.signingKey= Keys.hmacShaKeyFor(
                Base64.getEncoder().
                        withoutPadding().encode(properties.secretKey().getBytes(StandardCharsets.UTF_8))
        );
        this.userDetailsService = userDetailsService;
    }


    // Create token & refresh token
    public static String createToken(
            Authentication authentication,
            JwtProperties properties
    ){
        SecretKey signingKey = Keys.hmacShaKeyFor(
                Base64.getEncoder().
                        withoutPadding().encode(properties.secretKey().getBytes(StandardCharsets.UTF_8))
        );
        Instant issuedAt = Instant.now();
        Date expirationDate = getExpirationDate(issuedAt, properties.expirationMinutes());

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        staticLog.info(authentication.getName());

        var builder = Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(Date.from(issuedAt))
                .claim("auth", authorities)
                .expiration(expirationDate);

        if (authentication.getPrincipal() instanceof CustomUserDetails details) {
            Auth user = details.getUser();
            builder.claim("userId", user.getId());
            if (user.getDisplayName() != null) {
                builder.claim("displayName", user.getDisplayName());
            }
        }

        return builder.signWith(signingKey).compact();
    }

    public static String createRefreshToken(
            Authentication authentication,
            JwtProperties properties
    ){
        SecretKey signingKey = Keys.hmacShaKeyFor(
                Base64.getEncoder().
                        withoutPadding().encode(properties.secretKey().getBytes(StandardCharsets.UTF_8))
        );
        Instant issuedAt = Instant.now();
        Date expirationDate = getExpirationDate(issuedAt, properties.refreshExpirationMinutes());

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(Date.from(issuedAt))
                .claim("auth", authorities)
                .claim("typ", "refresh")
                .expiration(expirationDate)
                .signWith(signingKey)
                .compact();
    }

    private static Date getExpirationDate(Instant issuedAt, int expirationMinutes) {
        Instant expriationInstant = issuedAt
                .plus(expirationMinutes, ChronoUnit.MINUTES);
        return Date.from(expriationInstant);
    }



    // Get Authentication Information based on token
    public Authentication getAuthentication(String token){

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }


    // Check validation of token
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;

        } catch (ExpiredJwtException e) {
            staticLog.warn("JWT expired at {}", e.getClaims().getExpiration());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.expired");

        } catch (MalformedJwtException e) {
            staticLog.warn("JWT malformed: {}", e.getMessage());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.malformed");

        } catch (SecurityException | WeakKeyException | IllegalArgumentException e) {
            // SecurityException covers invalid signature, IllegalArgumentException for empty/invalid token
            staticLog.warn("JWT invalid: {}", e.getMessage());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");

        } catch (Exception e) {
            staticLog.error("Unexpected JWT validation error", e);
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }
    }

    public boolean validateRefreshToken(String token) {
        if (Objects.isNull(token)) {
            return false;
        }

        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}