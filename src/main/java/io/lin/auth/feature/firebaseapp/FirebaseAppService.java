package io.lin.auth.feature.firebaseapp;

import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.firebaseapp.dto.AppLoginResponse;
import io.lin.auth.feature.firebaseapp.dto.AppRegisterRequest;
import io.lin.auth.feature.firebaseapp.dto.AppSocialProfile;
import io.lin.auth.feature.firebaseapp.dto.VerifiedFirebaseIdentity;
import io.lin.auth.feature.login.jwt.JwtProperties;
import io.lin.auth.feature.login.jwt.aUserDetails.CustomUserDetails;
import io.lin.auth.feature.login.jwt.bToken.TokenProvider;
import io.lin.auth.storage.R2Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FirebaseAppService {

    private final AuthRepo authRepo;
    private final PasswordEncoder encoder;
    private final R2Storage r2Storage;
    private final JwtProperties jwtProperties;

    @Transactional
    public AppLoginResponse appLogin(VerifiedFirebaseIdentity identity) {
        Auth userLogin = authRepo.findByUid(identity.uid()).orElse(null);
        if (userLogin != null) {
            return loginSuccess(userLogin);
        }

        Auth userByEmail = authRepo.findByEmail(identity.email()).orElse(null);
        if (userByEmail != null) {
            return linkFirebaseUidAndLogin(userByEmail, identity);
        }

        return AppLoginResponse.needRegister(AppSocialProfile.fromIdentity(identity));
    }

    private AppLoginResponse linkFirebaseUidAndLogin(Auth user, VerifiedFirebaseIdentity identity) {
        requireVerifiedSocialEmail(identity);

        String existingUid = user.getUid();
        if (existingUid != null && !existingUid.isBlank()) {
            if (!Objects.equals(existingUid, identity.uid())) {
                throw new CustomException(HttpStatus.CONFLICT, "error.auth.firebase_already_linked");
            }
            return loginSuccess(user);
        }

        user.setUid(identity.uid());
        authRepo.save(user);
        return loginSuccess(user);
    }

    @Transactional
    public AppLoginResponse newAccount(VerifiedFirebaseIdentity identity, AppRegisterRequest request) {
        requireVerifiedSocialEmail(identity);

        if (authRepo.existsByUid(identity.uid())) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.account_already_registered");
        }
        if (authRepo.existsByUsername(request.username())) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.username_conflict");
        }
        if (authRepo.existsByEmail(identity.email())) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");
        }
        if (!Objects.equals(request.password(), request.cfPassword())) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.password.mismatch");
        }

        String displayName = StringUtils.hasText(request.displayName())
                ? request.displayName().trim()
                : identity.displayName();

        Auth user = Auth.builder()
                .username(request.username())
                .displayName(displayName)
                .password(encoder.encode(request.password()))
                .email(identity.email())
                .phone(request.phone())
                .uid(identity.uid())
//                .avatar("profiles/linlanga_logo.png")
                .build();

        authRepo.save(user);
        user.ensureSelfCreatedBy();
        authRepo.save(user);

        return loginSuccess(user);
    }

    private void requireVerifiedSocialEmail(VerifiedFirebaseIdentity identity) {
        if (!identity.emailVerified()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified");
        }
    }

    private AppLoginResponse loginSuccess(Auth user) {
        user.setLastLogin(LocalDateTime.now());
        authRepo.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String accessToken = TokenProvider.createToken(authentication, jwtProperties);
        String refreshToken = TokenProvider.createRefreshToken(authentication, jwtProperties);

        return AppLoginResponse.success(
                UserInfo.fromEntity(user, resolveAvatar(user)),
                accessToken,
                refreshToken
        );
    }

    private String resolveAvatar(Auth user) {
        return user.getAvatar() != null ? r2Storage.publicUrl(user.getAvatar()) : null;
    }
}
