# Lin Auth API — Feature docs

Base URL (local): `http://localhost:8081`

| Feature | Doc | Base path |
|---------|-----|-----------|
| Email verification | [email-verification.md](./email-verification.md) | `/auth/email-verification` |
| Sign up | [signup.md](./signup.md) | `/auth/sign-up` |
| Login | [login.md](./login.md) | `/auth/login`, `/auth/find-*` |
| Firebase app | [firebase-app.md](./firebase-app.md) | `/auth/app` |
| Profile | [profile.md](./profile.md) | `/auth` (authenticated) |
| Shared types | [account.md](./account.md) | — |

## Common error response

Business errors (`CustomException`):

```json
{
  "timestamp": "2026-09-12T09:00:00",
  "status": 409,
  "error": "CONFLICT",
  "message": "Human-readable localized message",
  "path": "/auth/sign-up"
}
```

Validation errors (`400`):

```json
{
  "timestamp": "2026-09-12T09:00:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/auth/sign-up",
  "fields": {
    "email": "Please enter a valid email address."
  }
}
```

## Authentication header

Protected endpoints require:

```http
Authorization: Bearer {accessToken}
```

Login also sets the same value in the response header `Authorization`.
