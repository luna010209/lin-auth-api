package io.lin.auth.feature.admin.dto;

import java.time.LocalDate;

public record AdminUserStatsResponse(
        long totalUsers,
        long registeredInPeriod,
        long firebaseLinkedUsers,
        LocalDate from,
        LocalDate to
) {
}
