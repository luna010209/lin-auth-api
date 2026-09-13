package io.lin.auth.feature.admin;

import io.lin.auth.common.dto.ApiResponse;
import io.lin.auth.feature.admin.dto.AdminUserOverviewResponse;
import io.lin.auth.feature.admin.dto.AdminUserStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/overview")
    @Operation(
            summary = "User KPI snapshot",
            description = "Total users, new users this month, and Firebase-linked users. Requires ROLE_ADMIN."
    )
    public ResponseEntity<ApiResponse<AdminUserOverviewResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.overview()));
    }

    @GetMapping
    @Operation(
            summary = "User registration stats",
            description = "Total users and optional registration count for a custom date range. Requires ROLE_ADMIN."
    )
    public ResponseEntity<ApiResponse<AdminUserStatsResponse>> userStats(
            @Parameter(description = "Start date (inclusive). Use together with `to`.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End date (inclusive). Use together with `from`.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.userStats(from, to)));
    }
}
