package io.lin.auth.feature.firebaseapp.dto;

public record AppSocialProfile(
        String uid,
        String email,
        String displayName,
        String picture
) {
    public static AppSocialProfile fromIdentity(VerifiedFirebaseIdentity identity) {
        return new AppSocialProfile(
                identity.uid(),
                identity.email(),
                identity.displayName(),
                identity.picture()
        );
    }
}
