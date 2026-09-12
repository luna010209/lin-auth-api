# Firebase app

Flutter/mobile flows after Firebase Authentication (Google, etc.). Links Firebase UID to Lin accounts.

**Package:** `io.lin.auth.feature.firebaseapp`  
**Auth:** Public (no JWT)

---

## Shared response type — `AppLoginResponse`

Used by `POST /auth/app`, `/confirm`, and `/register`.

```json
{
  "user": {
    "id": 1,
    "username": "luna",
    "displayName": "Luna",
    "email": "user@example.com",
    "phone": null,
    "uid": "firebase-uid-abc",
    "avatar": "https://cdn.example.com/profiles/1/avatar.jpg",
    "lastLogin": null,
    "createdAt": "2026-09-12T10:00:00",
    "roles": []
  },
  "status": "LOGIN_SUCCESS"
}
```

| Field | Type | Notes |
|-------|------|-------|
| `user` | [UserInfo](./account.md#userinfo) \| null | `null` when `status` is `NEED_REGISTER` |
| `status` | enum | `LOGIN_SUCCESS`, `EMAIL_EXIST`, `NEED_REGISTER` |

### `AppLoginStatus`

| Value | Meaning | Next step |
|-------|---------|-----------|
| `LOGIN_SUCCESS` | User found / linked / registered | Use `user` in app |
| `EMAIL_EXIST` | Firebase UID new, but email matches existing web account | Call `/auth/app/confirm` |
| `NEED_REGISTER` | No UID and no email match | Call `/auth/app/register` (after email verified) |

---

## POST `/auth/app`

Check login status after Firebase sign-in.

### Request

**Content-Type:** `application/json`

```json
{
  "uid": "firebase-uid-abc",
  "email": "user@example.com"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `uid` | string | Yes | Firebase UID |
| `email` | string | No | Used when UID not yet linked |

### Response

**200 OK** — `AppLoginResponse` (see above)

Examples:

**UID already linked:**

```json
{
  "user": { "...": "..." },
  "status": "LOGIN_SUCCESS"
}
```

**Email exists, UID new:**

```json
{
  "user": { "...": "..." },
  "status": "EMAIL_EXIST"
}
```

**New user:**

```json
{
  "user": null,
  "status": "NEED_REGISTER"
}
```

---

## POST `/auth/app/confirm`

Link Firebase UID to an existing web account (username + password proof).

### Request

```json
{
  "uid": "firebase-uid-abc",
  "email": "user@example.com",
  "username": "luna",
  "password": "Secret123!"
}
```

| Field | Type | Required |
|-------|------|----------|
| `uid` | string | Yes |
| `email` | string | Yes |
| `username` | string | Yes |
| `password` | string | Yes |

### Response

**200 OK** — `AppLoginResponse` with `status: "LOGIN_SUCCESS"`

### Errors

| HTTP | Code | When |
|------|------|------|
| 404 | `error.auth.email_not_found` | Email not found |
| 401 | `error.auth.invalid_credentials` | Username/password mismatch |
| 409 | `error.auth.firebase_already_linked` | Account linked to different UID |

---

## POST `/auth/app/register`

Create account with Firebase UID (requires verified email).

### Request

```json
{
  "uid": "firebase-uid-abc",
  "username": "luna",
  "password": "Secret123!",
  "cfPassword": "Secret123!",
  "email": "user@example.com",
  "displayName": "Luna",
  "phone": "01012345678"
}
```

| Field | Type | Required |
|-------|------|----------|
| `uid` | string | Yes |
| `username` | string | Yes |
| `password` | string | Yes |
| `cfPassword` | string | Yes |
| `email` | string | Yes |
| `displayName` | string | No |
| `phone` | string | No |

### Response

**200 OK** — `AppLoginResponse` with `status: "LOGIN_SUCCESS"` and new `user`

Default avatar key: `profiles/linlanga_logo.png`

### Errors

| HTTP | Code | When |
|------|------|------|
| 409 | `error.auth.account_already_registered` | UID already used |
| 409 | `error.auth.username_conflict` | Username taken |
| 409 | `error.auth.email_conflict` | Email taken |
| 409 | `error.auth.password.mismatch` | Passwords differ |
| 400 | `error.auth.email.not_verified` | Email not verified |

---

## POST `/auth/app/verify-email`

Mark email as verified for social login (skip code flow when email comes from Google, etc.).

### Request

| Type | Name | Required |
|------|------|----------|
| Query | `email` | Yes |

```http
POST /auth/app/verify-email?email=user@example.com
```

### Response

**200 OK** — empty body

Creates or updates `email_verification` with `verified=true`.

---

## Typical app flow

```text
Firebase sign-in
  → POST /auth/app { uid, email }
      LOGIN_SUCCESS     → done
      EMAIL_EXIST       → POST /auth/app/confirm
      NEED_REGISTER     → POST /auth/app/verify-email?email=... (social)
                         → POST /auth/app/register
```
