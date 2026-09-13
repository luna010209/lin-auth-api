package io.lin.auth.config.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.firebaseapp.dto.VerifiedFirebaseIdentity;
import io.lin.auth.feature.login.jwt.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseTokenVerifier {

    private final UserDetailsService userDetailsService;
    private final AuthRepo authRepo;

    public VerifiedFirebaseIdentity verifyIdentity(String authorizationHeader) {
        return verifyIdentityFromToken(requireFirebaseIdToken(authorizationHeader));
    }

    public VerifiedFirebaseIdentity verifyIdentityFromToken(String idToken) {
        if (!TokenUtil.isFirebaseToken(idToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            FirebaseToken decoded = FirebaseAuth.getInstance(app).verifyIdToken(idToken);

            String email = decoded.getEmail();
            if (!StringUtils.hasText(email)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "error.auth.email_not_found");
            }

            return new VerifiedFirebaseIdentity(
                    decoded.getUid(),
                    email.trim().toLowerCase(),
                    decoded.getName(),
                    decoded.getPicture(),
                    decoded.isEmailVerified()
            );
        } catch (FirebaseAuthException e) {
            log.error("Invalid Firebase token: {} / {}", e.getAuthErrorCode(), e.getMessage());
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.invalid");
        }
    }

    public Authentication getAuth(String idToken) {
        VerifiedFirebaseIdentity identity = verifyIdentityFromToken(idToken);

        Auth user = authRepo.findByUid(identity.uid()).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND, "No exist account in system")
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String requireFirebaseIdToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.missing");
        }

        String trimmed = authorizationHeader.trim();
        if (!trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.missing");
        }

        String idToken = trimmed.substring(7).trim();
        if (!StringUtils.hasText(idToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "error.token.missing");
        }

        return idToken;
    }
}


