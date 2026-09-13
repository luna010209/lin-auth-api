# Account (shared types)

Shared user model and DTOs used across features. No dedicated HTTP endpoints.

**Package:** `io.lin.auth.feature.account`

---

## Database

| Table | Entity |
|-------|--------|
| `auth` | `Auth` |
| `auth_roles` | `Auth.roles` (element collection) |

See [lin-auth-schema.sql](../lin-auth-schema.sql).

---

## UserInfo

Returned by [Login](./login.md) (via JWT claims context), [Profile GET](./profile.md#get-auth), and [Firebase app](./firebase-app.md) responses.

```json
{
  "id": 1,
  "username": "luna",
  "displayName": "Luna",
  "email": "user@example.com",
  "phone": "01012345678",
  "uid": "firebase-uid-or-null",
  "avatar": "https://cdn.example.com/profiles/1/avatar.jpg",
  "lastLogin": "2026-09-12T10:30:00",
  "createdAt": "2026-09-12T09:00:00",
  "roles": ["ROLE_TEACHER", "ROLE_ADMIN"]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | long | Primary key |
| `username` | string | Login id |
| `displayName` | string | Display name |
| `email` | string | Unique email |
| `phone` | string \| null | Optional phone |
| `uid` | string \| null | Firebase UID when linked |
| `avatar` | string \| null | Public URL (resolved from R2 key) |
| `lastLogin` | datetime \| null | Last web login time |
| `createdAt` | datetime | Account creation time |
| `roles` | string[] | e.g. `ROLE_TEACHER`, `ROLE_ADMIN` |

---

## ChangeInfoRequest

Used by [Profile PUT `/auth`](./profile.md#put-auth).

```json
{
  "username": "string",
  "email": "string",
  "displayName": "string",
  "phone": "string"
}
```

---

## ChangePasswordRequest

Used by [Profile PUT `/auth/change-password`](./profile.md#put-authchange-password).

```json
{
  "currentPassword": "string",
  "newPassword": "string",
  "cfPassword": "string"
}
```

---

## Role enum

| Constant | Authority string |
|----------|------------------|
| `ROLE_TEACHER` | `ROLE_TEACHER` |
| `ROLE_ADMIN` | `ROLE_ADMIN` |

### Assign roles (SQL)

Roles live in `auth_roles`, not on the `auth` row:

```sql
INSERT IGNORE INTO auth_roles (auth_id, roles)
VALUES (1, 'ROLE_ADMIN');
```

Use the exact enum string (`ROLE_ADMIN`, not `ADMIN`). After inserting or changing roles, **log in again** so the JWT `auth` claim and `GET /auth` → `roles` array are refreshed.

### Entity note

`Auth.roles` is a mutable `Set<Role>` (`@ElementCollection`, eager). Do not use a `final` collection field — Hibernate may fail to load roles and an login `save()` can sync an empty set and delete `auth_roles` rows.

---

## JWT (login feature)

Token creation and validation live under `io.lin.auth.feature.login.jwt`. See [login.md](./login.md).
