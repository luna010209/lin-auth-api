package io.lin.auth.feature.emailverification.repo;

import io.lin.auth.feature.emailverification.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepo extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmail(String email);

}
