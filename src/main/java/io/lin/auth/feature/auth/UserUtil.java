package io.lin.auth.feature.auth;

import io.lin.auth.feature.auth.entity.Auth;
import io.lin.auth.feature.auth.repo.AuthRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class UserUtil {
    private final AuthRepo authRepo;
    public final String AUTH_SESSION_KEY = "AUTHENTICATION";

    public Auth currentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication ==null || !authentication.isAuthenticated()) return null;

        String username = authentication.getName();
        return authRepo.findByUsername(username).orElse(null);
    }
}
