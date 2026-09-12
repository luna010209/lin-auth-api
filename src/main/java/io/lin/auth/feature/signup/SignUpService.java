package io.lin.auth.feature.signup;

import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.emailverification.EmailVerificationService;
import io.lin.auth.feature.emailverification.entity.EmailVerification;
import io.lin.auth.feature.signup.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final AuthRepo authRepo;
    private final PasswordEncoder encoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public void signUp(SignUpRequest request) {
        String email = request.email().trim().toLowerCase();

        if (authRepo.existsByUsername(request.username()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.username_conflict");
        if (authRepo.existsByEmail(email))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.email_conflict");
        if (!Objects.equals(request.password(), request.cfPassword()))
            throw new CustomException(HttpStatus.CONFLICT, "error.auth.password.mismatch");

        EmailVerification verification = emailVerificationService.requireVerified(email);

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

        emailVerificationService.delete(verification);
    }
}
