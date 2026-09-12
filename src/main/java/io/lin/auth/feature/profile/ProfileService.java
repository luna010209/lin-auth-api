package io.lin.auth.feature.profile;

import io.lin.auth.common.dto.FileInfo;
import io.lin.auth.feature.account.dto.ChangeInfoRequest;
import io.lin.auth.feature.account.dto.ChangePasswordRequest;
import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.emailverification.entity.EmailVerification;
import io.lin.auth.feature.account.enums.Role;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.emailverification.repo.EmailVerificationRepo;
import io.lin.auth.storage.R2Storage;
import io.lin.auth.feature.account.UserUtil;
import io.lin.auth.utils.file.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AuthRepo authRepo;
    private final EmailVerificationRepo emailVerificationRepo;

    private final PasswordEncoder encoder;
    private final UserUtil userUtil;

    private final R2Storage r2Storage;

    @Transactional
    public UserInfo userLogin() {
        Auth user = userUtil.currentUser();
        if (user == null) throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");
        String avatar = user.getAvatar() != null ?
                r2Storage.publicUrl(user.getAvatar()) : null;
        return UserInfo.fromEntity(user, avatar);
    }


    @Transactional
    public void changeInfo(ChangeInfoRequest request) {
        Auth user = userUtil.currentUser();
        if (user == null)
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");

        if (!Objects.equals(request.username(), user.getUsername())) {
            if (authRepo.existsByUsername(request.username()))
                throw new CustomException(HttpStatus.CONFLICT, "error.auth.username_conflict");

            user.setUsername(request.username());
        }

        if (!Objects.equals(request.displayName(), user.getDisplayName())) user.setDisplayName(request.displayName());
        if (!Objects.equals(request.phone(), user.getPhone())) user.setPhone(request.phone());

        String email = request.email().trim().toLowerCase();
        if (!Objects.equals(email, user.getEmail())) {
            if (authRepo.existsByEmail(email))
                throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");

            EmailVerification verification = emailVerificationRepo.findByEmail(email).orElseThrow(
                    () -> new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified")
            );
            if (!verification.isVerified())
                throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified");

            user.setEmail(email);
            if (user.getUid() != null) user.setUid(null);

            emailVerificationRepo.delete(verification);
        }

        authRepo.save(user);
    }


    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Auth currentUser = userUtil.currentUser();
        if (currentUser == null)
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");

        if (!encoder.matches(request.currentPassword(), currentUser.getPassword()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.current_password_invalid");

        if (!Objects.equals(request.newPassword(), request.cfPassword()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.password.mismatch");

        currentUser.setPassword(encoder.encode(request.newPassword()));
        authRepo.save(currentUser);
    }


    @Transactional
    public void changeAvatar(MultipartFile avatar) {
        Auth currentUser = userUtil.currentUser();
        if (currentUser == null)
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.auth.login_required");

        if (avatar.getSize() > 2 * 1024 * 1024) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.file.too_large", 2);
        }

        String contentType = avatar.getContentType();
        if (!FileUtil.isValid("image", contentType)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.image.invalid_type");
        }

        String avatarKey = "profiles/" + currentUser.getId() + "/avatar.jpg";

        try {
            FileInfo avatarInfo = FileUtil.processFile(avatar);

            r2Storage.putBytes(avatarKey, avatarInfo.contentType(), avatarInfo.fileByte());

        } catch (IOException e) {
            throw new CustomException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "error.file.upload_failed"
            );
        }

        currentUser.setAvatar(avatarKey);
        authRepo.save(currentUser);
    }


    public void upgradeRole(String userId, Role newRole) {

    }

}
