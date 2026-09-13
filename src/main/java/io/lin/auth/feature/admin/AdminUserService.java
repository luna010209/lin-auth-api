package io.lin.auth.feature.admin;

import io.lin.auth.exception.CustomException;
import io.lin.auth.feature.account.entity.Auth;
import io.lin.auth.feature.account.repo.AuthRepo;
import io.lin.auth.feature.admin.dto.AdminUserOverviewResponse;
import io.lin.auth.feature.admin.dto.AdminUserStatsResponse;
import io.lin.auth.feature.admin.dto.AuthSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthRepo authRepo;

    @Value("${app.billing.timezone:Asia/Seoul}")
    private String billingTimezone;

    @Transactional(readOnly = true)
    public AdminUserOverviewResponse overview() {
        ZoneId zone = ZoneId.of(billingTimezone);
        var monthRange = AdminUserStatsDateRange.currentMonth(LocalDate.now(zone));

        return new AdminUserOverviewResponse(
                authRepo.count(),
                authRepo.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        monthRange.fromDateTime(),
                        monthRange.toExclusive()
                ),
                authRepo.countFirebaseLinkedUsers()
        );
    }

    @Transactional(readOnly = true)
    public AdminUserStatsResponse userStats(LocalDate from, LocalDate to) {
        AdminUserStatsDateRange.Resolved range = AdminUserStatsDateRange.resolveOptional(from, to);

        long totalUsers = authRepo.count();
        long registeredInPeriod = range == null
                ? totalUsers
                : authRepo.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        range.fromDateTime(),
                        range.toExclusive()
                );

        return new AdminUserStatsResponse(
                totalUsers,
                registeredInPeriod,
                authRepo.countFirebaseLinkedUsers(),
                range != null ? range.from() : null,
                range != null ? range.to() : null
        );
    }

    @Transactional(readOnly = true)
    public void requireUserExists(Long userId) {
        if (!authRepo.existsById(userId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "error.auth.user_not_found");
        }
    }

    @Transactional(readOnly = true)
    public List<AuthSummary> lookupUsers(List<Long> ids) {
        return authRepo.findAllById(ids).stream()
                .map(this::toSummary)
                .toList();
    }

    private AuthSummary toSummary(Auth auth) {
        return new AuthSummary(auth.getId(), auth.getDisplayName());
    }
}
