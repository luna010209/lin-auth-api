package io.lin.auth.feature.emailverification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "email_verification")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "verified_code", nullable = false, length = 100)
    @Setter
    private String verifiedCode;

    @Column(name = "expires_at", nullable = false)
    @Setter
    private LocalDateTime expiration;

    @Column(name = "is_verified", nullable = false)
    @Setter
    private boolean verified;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
