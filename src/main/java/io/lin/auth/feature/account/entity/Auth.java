package io.lin.auth.feature.account.entity;

import io.lin.auth.common.audit.UserAuditable;
import io.lin.auth.feature.account.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table(name = "auth")
public class Auth extends UserAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 25)
    private String phone;

    @Column(name = "display_name", length = 150)
    private String displayName;
    
    @Setter
    private String avatar;

    @Column(name = "firebase_uid", unique = true)
    private String uid;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "auth_roles", joinColumns = @JoinColumn(name = "auth_id"))
    @Column(name = "roles")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private final Set<Role> roles = new HashSet<>();

    public void ensureSelfCreatedBy() {
        assignCreatedByIfAbsent(id);
    }

}
