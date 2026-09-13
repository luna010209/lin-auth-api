# Firebase app

Google / Firebase sign-in from the Flutter app (or any client). Links Firebase UID to Lin accounts.

**Package:** `io.lin.auth.feature.firebaseapp`  
**Auth:** Public endpoints, but every request requires a **verified Firebase ID token** in the `Authorization` header.

---

## Client flow (Google → Firebase → LinAuth)

```text
1. Client: Firebase Auth signInWithGoogle()
2. Client: idToken = await user.getIdToken()
3. Client: POST /auth/app
           Authorization: Bearer {idToken}
4. Handle status:
     LOGIN_SUCCESS  → use user + JWT (or same idToken for API calls)
     NEED_REGISTER  → POST /auth/app/register (no email_verification table)
```

When email already exists in LinAuth but `firebase_uid` is empty, `POST /auth/app` **auto-links** the UID and returns `LOGIN_SUCCESS` (no password proof required).

`uid` and `email` always come from the **verified token** on the server — do not send them in the body.

---

## Shared header

All `/auth/app/*` endpoints:

```http
Authorization: Bearer {firebaseIdToken}
```

Get `firebaseIdToken` from Firebase Auth after Google sign-in (`user.getIdToken()` in Flutter).

---

## Shared response type — `AppLoginResponse`

Used by `POST /auth/app` and `/register`.

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
  "status": "LOGIN_SUCCESS",
  "profile": null,
  "accessToken": "...",
  "refreshToken": "..."
}
```

When `status` is `NEED_REGISTER`, `user` is `null` and `profile` is pre-filled from the Google/Firebase token:

```json
{
  "user": null,
  "status": "NEED_REGISTER",
  "profile": {
    "uid": "firebase-uid-abc",
    "email": "user@gmail.com",
    "displayName": "Luna Kim",
    "picture": "https://lh3.googleusercontent.com/..."
  }
}
```

| Field | Type | Notes |
|-------|------|-------|
| `user` | [UserInfo](./account.md#userinfo) \| null | `null` when `status` is `NEED_REGISTER` |
| `status` | enum | `LOGIN_SUCCESS`, `NEED_REGISTER` |
| `profile` | object \| null | Pre-filled Google data when `NEED_REGISTER`; use to populate the registration form |

### `AppLoginStatus`

| Value | Meaning | Next step |
|-------|---------|-----------|
| `LOGIN_SUCCESS` | User found, auto-linked, or registered | Use JWT + `user` in app |
| `NEED_REGISTER` | No UID and no email match | Show registration form (pre-fill from `profile`), then `POST /auth/app/register` |

---

## POST `/auth/app`

Check login status after Firebase Google sign-in.

### Request

```http
POST /auth/app
Authorization: Bearer {firebaseIdToken}
```

No request body.

### Server logic

1. `firebase_uid` found → `LOGIN_SUCCESS`
2. Email found, `firebase_uid` empty → set uid from token → `LOGIN_SUCCESS`
3. Email found, different `firebase_uid` → `409 error.auth.firebase_already_linked`
4. No match → `NEED_REGISTER` with `profile`

### Response

**200 OK** — `AppLoginResponse` (see above)

---

## POST `/auth/app/register`

Create account with Firebase UID (requires verified email on token).

### Request

```http
POST /auth/app/register
Authorization: Bearer {firebaseIdToken}
Content-Type: application/json
```

```json
{
  "username": "luna",
  "password": "Secret123!",
  "cfPassword": "Secret123!",
  "displayName": "Luna",
  "phone": "01012345678"
}
```

| Field | Type | Required |
|-------|------|----------|
| `username` | string | Yes |
| `password` | string | Yes |
| `cfPassword` | string | Yes |
| `displayName` | string | No (falls back to Google name from token) |
| `phone` | string | No |

`email` and `firebase_uid` come from the Firebase ID token.

Google registration trusts `email_verified=true` on the Firebase token. Does **not** use the `email_verification` table.

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
| 400 | `error.auth.email.not_verified` | Token email not verified (non-Google providers) |

---

## Typical app flow

```text
Firebase Google sign-in → getIdToken()
  → POST /auth/app
      LOGIN_SUCCESS     → done (JWT returned)
      NEED_REGISTER     → show form (pre-fill from profile.email, profile.displayName)
                         → POST /auth/app/register { username, password, ... }
```

---

## Example — check login

```bash
curl -X POST http://localhost:8081/auth/app \
  -H "Authorization: Bearer FIREBASE_ID_TOKEN"
```
