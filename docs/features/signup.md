# Sign up

Create a web account after email verification.

**Package:** `io.lin.auth.feature.signup`  
**Auth:** Public (no JWT)

**Prerequisite:** [Email verification](./email-verification.md) completed for the same email.

---

## POST `/auth/sign-up`

### Request

**Content-Type:** `application/json`

```json
{
  "username": "luna",
  "displayName": "Luna",
  "password": "Secret123!",
  "cfPassword": "Secret123!",
  "email": "user@example.com",
  "phone": "01012345678"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `username` | string | Yes | Must be unique |
| `displayName` | string | Yes | |
| `password` | string | Yes | Stored BCrypt-encoded |
| `cfPassword` | string | Yes | Must match `password` |
| `email` | string | Yes | Must be verified; normalized to lowercase |
| `phone` | string | No | Max 25 chars in DB |

### Response

**200 OK** — empty body

Side effects:

- Creates row in `auth` table
- Sets `created_by` to the new user id
- Deletes the `email_verification` row for that email

### Errors

| HTTP | Code | When |
|------|------|------|
| 409 | `error.auth.username_conflict` | Username taken |
| 409 | `error.auth.email_conflict` | Email already registered |
| 409 | `error.auth.password.mismatch` | `password` ≠ `cfPassword` |
| 400 | `error.auth.email.not_verified` | Email not verified |
| 400 | `fields.*` | Bean validation on required fields |

---

## Example

```bash
curl -X POST http://localhost:8081/auth/sign-up \
  -H "Content-Type: application/json" \
  -d '{
    "username": "luna",
    "displayName": "Luna",
    "password": "Secret123!",
    "cfPassword": "Secret123!",
    "email": "user@example.com",
    "phone": "01012345678"
  }'
```
