package io.lin.auth.feature.firebaseapp.dto;

public record VerifiedFirebaseIdentity(
        String uid,
        String email,
        String displayName,
        String picture,
        boolean emailVerified
) {
}
