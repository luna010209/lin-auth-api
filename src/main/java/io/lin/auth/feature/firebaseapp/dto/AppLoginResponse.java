package io.lin.auth.feature.firebaseapp.dto;

import io.lin.auth.feature.account.dto.UserInfo;
import io.lin.auth.feature.firebaseapp.enums.AppLoginStatus;

public record AppLoginResponse(
        UserInfo user,
        AppLoginStatus status
) {
}
