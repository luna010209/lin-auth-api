package io.lin.auth.feature.admin;

import io.lin.auth.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AdminUserStatsDateRange {

    private AdminUserStatsDateRange() {
    }

    public record Resolved(
            LocalDate from,
            LocalDate to,
            LocalDateTime fromDateTime,
            LocalDateTime toExclusive
    ) {
    }

    public static Resolved currentMonth(LocalDate today) {
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return build(start, end);
    }

    public static Resolved resolveOptional(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        if (from == null || to == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.admin_stats.date_range_required");
        }
        if (to.isBefore(from)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "error.admin_stats.invalid_date_range");
        }
        return build(from, to);
    }

    private static Resolved build(LocalDate from, LocalDate to) {
        return new Resolved(
                from,
                to,
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay()
        );
    }
}
