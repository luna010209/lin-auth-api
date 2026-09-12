package io.lin.auth.feature.account.repo;

import io.lin.auth.feature.account.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthRepo extends JpaRepository<Auth, Long> {

    Optional<Auth> findByUsername(String username);

    Optional<Auth> findByUid(String uid);

    Optional<Auth> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByUid(String uid);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime toExclusive);

    @Query("SELECT COUNT(a) FROM Auth a WHERE a.uid IS NOT NULL")
    long countFirebaseLinkedUsers();

}
