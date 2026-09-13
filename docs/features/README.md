# Lin Auth API — Feature docs

Base URL (local): `http://localhost:8081`

| Feature | Doc | Base path |
|---------|-----|-----------|
| Email verification | [email-verification.md](./email-verification.md) | `/auth/email-verification` |
| Sign up | [signup.md](./signup.md) | `/auth/sign-up` |
| Login | [login.md](./login.md) | `/auth/login`, `/auth/find-*` |
| Firebase app | [firebase-app.md](./firebase-app.md) | `/auth/app` |
| Google login | [google-login.md](google-login.md) | Firebase / Google Cloud |
| Profile | [profile.md](./profile.md) | `/auth` (authenticated) |
| Admin user stats | [admin-users.md](./admin-users.md) | `/auth/admin/users` |
| Shared types | [account.md](./account.md) | — |

## Response envelope — `ApiResponse<T>`

All endpoints return the same JSON envelope (`io.lin.auth.common.dto.ApiResponse`).

### Success (with data)

```json
{
  "success": true,
  "code": 200,
  "message": "Request processed successfully.",
  "data": { }
}
```

### Success (no data)

`data` and `error` are omitted (`@JsonInclude(NON_NULL)`):

```json
{
  "success": true,
  "code": 200,
  "message": "Request processed successfully."
}
```

### Error (business)

HTTP status matches `code`:

```json
{
  "success": false,
  "code": 409,
  "message": "This email is already in use.",
  "error": {
    "path": "/auth/sign-up"
  }
}
```

### Error (validation — 400)

```json
{
  "success": false,
  "code": 400,
  "message": "Validation failed",
  "error": {
    "path": "/auth/sign-up",
    "fields": {
      "email": "Please enter a valid email address."
    }
  }
}
```

### Paginated lists (future)

`data` will use `CursorPageResponse<T>`:

```json
{
  "success": true,
  "code": 200,
  "message": "Request processed successfully.",
  "data": {
    "items": [],
    "nextCursor": 42,
    "hasNext": true
  }
}
```

## Authentication header

Protected endpoints require:

```http
Authorization: Bearer {accessToken}
```

Login also sets the same value in the response header `Authorization`. Token payload is in `data.accessToken` / `data.refreshToken`.
