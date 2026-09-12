package io.lin.auth.feature.login;

import io.lin.auth.feature.login.jwt.JwtProperties;
import io.lin.auth.feature.login.jwt.aUserDetails.CustomUserDetails;
import io.lin.auth.feature.login.jwt.bToken.TokenProvider;
import io.lin.auth.feature.login.dto.LoginRequest;
import io.lin.auth.feature.login.dto.LoginResponse;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.login.mail.PasswordListener;
import io.lin.auth.feature.login.mail.PasswordListenerEvent;
import io.lin.auth.feature.login.mail.UsernameListener;
import io.lin.auth.feature.login.mail.UsernameListenerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthRepo authRepo;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder encoder;

    private final UsernameListenerEvent usernameEvent;
    private final PasswordListenerEvent passwordEvent;

    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
            );

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            Auth user = userDetails.getUser();

            user.setLastLogin(LocalDateTime.now());
            authRepo.save(user);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = TokenProvider.createToken(authentication, jwtProperties);
            String refreshToken = TokenProvider.createRefreshToken(authentication, jwtProperties);

            return new LoginResponse(accessToken, refreshToken);
        } catch (AuthenticationException e){
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.invalid_credentials");
        }
    }


    @Transactional
    public void findUsername(String email) {
        Auth auth = authRepo.findByEmail(email).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND, "error.auth.email_not_found")
        );

        usernameEvent.onApplicationEvent(new UsernameListener(email, auth.getUsername()));
    }

    public void findPassword(String email) {
        Auth auth = authRepo.findByEmail(email).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND, "error.auth.email_not_found")
        );

        String pool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            int index = random.nextInt(pool.length());
            password.append(pool.charAt(index));
        }

        auth.setPassword(encoder.encode(password));
        authRepo.save(auth);

        passwordEvent.onApplicationEvent(new PasswordListener(email, auth.getUsername(), password.toString()));
    }
}
