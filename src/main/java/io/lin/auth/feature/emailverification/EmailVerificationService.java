package io.lin.auth.feature.emailverification;

import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.emailverification.dto.EmailVerificationRequest;
import io.lin.auth.feature.emailverification.entity.EmailVerification;
import io.lin.auth.feature.emailverification.mail.EmailVerifyListener;
import io.lin.auth.feature.emailverification.mail.EmailVerifyListenerEvent;
import io.lin.auth.feature.emailverification.repo.EmailVerificationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepo emailVerificationRepo;
    private final AuthRepo authRepo;
    private final EmailVerifyListenerEvent verifyListenerEvent;

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
    public void markVerifiedForSocialLogin(String email) {
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

    @Transactional(readOnly = true)
    public EmailVerification requireVerified(String email) {
        EmailVerification verification = emailVerificationRepo.findByEmail(email).orElseThrow(
                () -> new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified")
        );
        if (!verification.isVerified())
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email.not_verified");
        return verification;
    }

    @Transactional
    public void delete(EmailVerification verification) {
        emailVerificationRepo.delete(verification);
    }
}
