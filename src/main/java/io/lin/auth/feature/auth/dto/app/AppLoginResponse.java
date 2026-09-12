package io.lin.auth.feature.auth.dto.app;

import io.lin.auth.feature.auth.dto.UserInfo;
import io.lin.auth.feature.auth.enums.AppLoginStatus;

public record AppLoginResponse(
        UserInfo user,
        AppLoginStatus status
) {
}
