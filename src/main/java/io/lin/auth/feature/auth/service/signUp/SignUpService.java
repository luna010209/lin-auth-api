package io.lin.auth.feature.auth.service.signUp;

import io.lin.auth.feature.auth.dto.signUp.EmailVerificationRequest;
import io.lin.auth.feature.auth.dto.signUp.SignUpRequest;
import io.lin.auth.feature.auth.entity.Auth;
import io.lin.auth.feature.auth.entity.EmailVerification;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.auth.repo.AuthRepo;
import io.lin.auth.feature.auth.repo.EmailVerificationRepo;
import io.lin.auth.feature.auth.UserUtil;
import io.lin.auth.feature.auth.service.sendMail.EmailVerifyListener;
import io.lin.auth.feature.auth.service.sendMail.EmailVerifyListenerEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final EmailVerificationRepo emailVerificationRepo;
    private final AuthRepo authRepo;

    private final PasswordEncoder encoder;

    private final EmailVerifyListenerEvent verifyListenerEvent;
    private final UserUtil userUtil;

    @Transactional
    public void sendMail(String email) {
        String finalEmail = email.trim().toLowerCase();

        if (authRepo.existsByEmail(finalEmail))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");

        Integer code = 100000 + new SecureRandom().nextInt(900000);

        EmailVerification verification = emailVerificationRepo.findByEmail(finalEmail)
                .orElseGet(() -> EmailVerification.builder()
                        .email(finalEmail)
                        .verified(false)
                        .build());

        verification.setVerifiedCode(String.valueOf(code));
        verification.setExpiration(LocalDateTime.now().plusMinutes(20));
        verification.setVerified(false);
        emailVerificationRepo.save(verification);

        verifyListenerEvent.onApplicationEvent(new EmailVerifyListener(email, code));

    }

    @Transactional
    public void verifyEmail(EmailVerificationRequest request) {

        String email = request.email().trim().toLowerCase();
        String code = request.verifiedCode().trim();

        if (authRepo.existsByEmail(email))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");

        EmailVerification verification = emailVerificationRepo.findByEmail(email).orElseThrow(
                () -> new CustomException(
                        HttpStatus.BAD_REQUEST,
                        "error.auth.verification.not_sent"
                )
        );

        if (verification.isVerified()) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_already_verified");
        }

        if (verification.getExpiration().isBefore(LocalDateTime.now()))
            throw new CustomException(HttpStatus.REQUEST_TIMEOUT, "error.auth.verification.expired");

        if (!Objects.equals(verification.getVerifiedCode(), code)) {
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.verification.invalid_code");
        }

        verification.setVerified(true);
        emailVerificationRepo.save(verification);

    }


    @Transactional
    public void signUp(SignUpRequest request) {
        String email = request.email().trim().toLowerCase();

        if (authRepo.existsByUsername(request.username()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.username_conflict");
        if (authRepo.existsByEmail(email))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");
        if (!Objects.equals(request.password(), request.cfPassword()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.password.mismatch");

        EmailVerification verification = emailVerificationRepo.findByEmail(email).orElseThrow(
                ()-> new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified")
        );
        if (!verification.isVerified())
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified");

        Auth user = Auth.builder()
                .username(request.username())
                .displayName(request.displayName())
                .password(encoder.encode(request.password()))
                .email(email)
                .phone(request.phone())
                .build();

        authRepo.save(user);
        user.ensureSelfCreatedBy();
        authRepo.save(user);

        emailVerificationRepo.delete(verification);
    }
}
