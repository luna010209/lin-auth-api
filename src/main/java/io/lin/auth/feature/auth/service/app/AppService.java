package io.lin.auth.feature.auth.service.app;

import io.lin.auth.feature.auth.dto.UserInfo;
import io.lin.auth.feature.auth.dto.app.AppConfirmRequest;
import io.lin.auth.feature.auth.dto.app.AppLoginRequest;
import io.lin.auth.feature.auth.dto.app.AppLoginResponse;
import io.lin.auth.feature.auth.dto.app.AppRegisterRequest;
import io.lin.auth.feature.auth.entity.Auth;
import io.lin.auth.feature.auth.entity.EmailVerification;
import io.lin.auth.feature.auth.enums.AppLoginStatus;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.auth.repo.AuthRepo;
import io.lin.auth.feature.auth.repo.EmailVerificationRepo;
import io.lin.auth.storage.R2Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppService {
    private final AuthRepo authRepo;
    private final PasswordEncoder encoder;
    private final EmailVerificationRepo emailVerificationRepo;
    private final R2Storage r2Storage;

    @Transactional
    public AppLoginResponse appLogin(AppLoginRequest request) {
        Auth userLogin = authRepo.findByUid(request.uid()).orElse(null);
        if (userLogin != null) {
            String avatar = userLogin.getAvatar() != null ?
                    r2Storage.publicUrl(userLogin.getAvatar()) : null;

            return new AppLoginResponse(
                    UserInfo.fromEntity(userLogin, avatar),
                    AppLoginStatus.LOGIN_SUCCESS
            );
        }

        if (!(request.email().isBlank() && request.email().isEmpty())) {
            Auth user = authRepo.findByEmail(request.email()).orElse(null);
            if (user != null) {
                return new AppLoginResponse(
                        UserInfo.fromEntity(user, null),
                        AppLoginStatus.EMAIL_EXIST
                );
            }
        }

        return new AppLoginResponse(
                null,
                AppLoginStatus.NEED_REGISTER
        );
    }


    @Transactional
    public AppLoginResponse confirmUser(AppConfirmRequest request) {
        Auth linked = authRepo.findByUid(request.uid()).orElse(null);
        if (linked != null) {
            String avatar = linked.getAvatar() != null ?
                    r2Storage.publicUrl(linked.getAvatar()) : null;

            return new AppLoginResponse(
                    UserInfo.fromEntity(linked, avatar),
                    AppLoginStatus.LOGIN_SUCCESS
            );
        }

        Auth userByEmail = authRepo.findByEmail(request.email()).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND, "error.auth.email_not_found")
        );

        if (!Objects.equals(userByEmail.getUsername(), request.username())
                || !encoder.matches(request.password(), userByEmail.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.invalid_credentials");
        }

        if (userByEmail.getUid() != null && !userByEmail.getUid().isBlank()) {
            if (!Objects.equals(userByEmail.getUid(), request.uid())) {
                throw new CustomException(HttpStatus.CONFLICT, "error.auth.firebase_already_linked");
            }
        } else {
            userByEmail.setUid(request.uid());
            authRepo.save(userByEmail);
        }

        String avatar = userByEmail.getAvatar() != null ?
                r2Storage.publicUrl(userByEmail.getAvatar()) : null;

        return new AppLoginResponse(
                UserInfo.fromEntity(userByEmail, avatar),
                AppLoginStatus.LOGIN_SUCCESS
        );
    }


    @Transactional
    public AppLoginResponse newAccount(AppRegisterRequest request) {
        if (authRepo.existsByUid(request.uid()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.account_already_registered");
        if (authRepo.existsByUsername(request.username()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.username_conflict");
        if (authRepo.existsByEmail(request.email()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");
        if (!Objects.equals(request.password(), request.cfPassword()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.password.mismatch");

        EmailVerification verification = emailVerificationRepo.findByEmail(request.email()).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified")
        );
        if (!verification.isVerified())
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified");

        Auth user = Auth.builder()
                .username(request.username())
                .displayName(request.displayName())
                .password(encoder.encode(request.password()))
                .email(request.email())
                .phone(request.phone())
                .uid(request.uid())
                .avatar("profiles/linlanga_logo.png")
                .build();

        authRepo.save(user);
        user.ensureSelfCreatedBy();
        authRepo.save(user);

        emailVerificationRepo.delete(verification);
        return new AppLoginResponse(
                UserInfo.fromEntity(user, null),
                AppLoginStatus.LOGIN_SUCCESS
        );
    }


    @Transactional
    public void verifyEmail(String email) {
        String finalEmail = email.trim().toLowerCase();

        EmailVerification verification = emailVerificationRepo.findByEmail(finalEmail)
                .orElseGet(() -> EmailVerification.builder()
                        .email(finalEmail)
                        .verified(true)
                        .build());

        if (verification.isVerified()) return;

        verification.setVerified(true);

        emailVerificationRepo.save(verification);
    }

}
