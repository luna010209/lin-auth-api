package io.lin.auth.feature.firebaseapp.dto;

import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.firebaseapp.enums.AppLoginStatus;

public record AppLoginResponse(
        UserInfo user,
        AppLoginStatus status,
        AppSocialProfile profile,
        String accessToken,
        String refreshToken
) {
    public static AppLoginResponse success(UserInfo user, String accessToken, String refreshToken) {
        return new AppLoginResponse(user, AppLoginStatus.LOGIN_SUCCESS, null, accessToken, refreshToken);
    }

    public static AppLoginResponse of(UserInfo user, AppLoginStatus status) {
        return new AppLoginResponse(user, status, null, null, null);
    }

    public static AppLoginResponse needRegister(AppSocialProfile profile) {
        return new AppLoginResponse(null, AppLoginStatus.NEED_REGISTER, profile, null, null);
    }
}
