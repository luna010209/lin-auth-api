package io.lin.auth.feature.firebaseapp;

import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.emailverification.EmailVerificationService;
import io.lin.auth.feature.emailverification.entity.EmailVerification;
import io.lin.auth.feature.firebaseapp.dto.AppConfirmRequest;
import io.lin.auth.feature.firebaseapp.dto.AppLoginRequest;
import io.lin.auth.feature.firebaseapp.dto.AppLoginResponse;
import io.lin.auth.feature.firebaseapp.dto.AppRegisterRequest;
import io.lin.auth.feature.firebaseapp.enums.AppLoginStatus;
import io.lin.auth.storage.R2Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FirebaseAppService {

    private final AuthRepo authRepo;
    private final PasswordEncoder encoder;
    private final EmailVerificationService emailVerificationService;
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

        EmailVerification verification = emailVerificationService.requireVerified(request.email());

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

        emailVerificationService.delete(verification);
        return new AppLoginResponse(
                UserInfo.fromEntity(user, null),
                AppLoginStatus.LOGIN_SUCCESS
        );
    }

    @Transactional
    public void verifyEmail(String email) {
        emailVerificationService.markVerifiedForSocialLogin(email);
    }
}
