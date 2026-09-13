# Admin user stats

Service: `io.lin.auth.feature.admin.AdminUserService`  
Base path: `/auth/admin/users`  
Access: `ROLE_ADMIN` only

User counts live in lin-auth-api (shared across Lin Universe products). Product APIs such as lin-langa do not expose user totals.

Timezone for month boundaries: `app.billing.timezone` (default `Asia/Seoul`).

---

## GET `/auth/admin/users/overview`

Dashboard snapshot for user KPIs.

**Response:** `ApiResponse<AdminUserOverviewResponse>`

| Field | Type | Description |
|-------|------|-------------|
| `totalUsers` | long | All registered users |
| `newUsersThisMonth` | long | `auth.created_at` in current calendar month |
| `firebaseLinkedUsers` | long | `firebase_uid IS NOT NULL` |

---

## GET `/auth/admin/users`

| Query | Type | Description |
|-------|------|-------------|
| `from` | LocalDate | Optional; inclusive start |
| `to` | LocalDate | Optional; inclusive end |

Both `from` and `to` must be provided together, or omitted for all-time.

**Response:** `ApiResponse<AdminUserStatsResponse>`

| Field | Type |
|-------|------|
| `totalUsers` | long |
| `registeredInPeriod` | long |
| `firebaseLinkedUsers` | long |
| `from` / `to` | LocalDate \| null |

---

## Errors

| Code | When |
|------|------|
| `error.admin_stats.date_range_required` | Only one of `from`/`to` provided |
| `error.admin_stats.invalid_date_range` | `to` before `from` |
