package io.lin.auth.feature.admin.dto;

public record AdminUserOverviewResponse(
        long totalUsers,
        long newUsersThisMonth,
        long firebaseLinkedUsers
) {
}
