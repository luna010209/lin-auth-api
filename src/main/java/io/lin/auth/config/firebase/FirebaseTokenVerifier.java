package io.lin.auth.config.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.repo.AuthRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

    private final UserDetailsService userDetailsService;
    private final AuthRepo authRepo;

    public Authentication getAuth(String idToken) {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseToken decoded = FirebaseAuth.getInstance(app).verifyIdToken(idToken);

            Auth user = authRepo.findByUid(decoded.getUid()).orElseThrow(
                    () -> new CustomException(HttpStatus.NOT_FOUND, "No exist account in system")
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            return authentication;
        } catch (FirebaseAuthException e) {
            log.error("Invalid Firebase token: " + e.getAuthErrorCode() + " / " + e.getMessage());
            throw new CustomException(
                    HttpStatus.UNAUTHORIZED,
                    "error.token.invalid"
            );
        }
    }
}


