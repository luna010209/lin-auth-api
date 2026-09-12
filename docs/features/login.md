# Login

Web login (JWT issuance) and account recovery emails.

**Package:** `io.lin.auth.feature.login` (+ `login.jwt` for tokens)  
**Auth:** Public (no JWT)

---

## POST `/auth/login`

Authenticates with username and password. Returns JWT access and refresh tokens.

### Request

**Content-Type:** `application/json`

```json
{
  "username": "luna",
  "password": "Secret123!"
}
```

| Field | Type | Required |
|-------|------|----------|
| `username` | string | Yes |
| `password` | string | Yes |

### Response

**200 OK**

**Headers:**

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**Body:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

| Field | Type | Description |
|-------|------|-------------|
| `accessToken` | string | JWT for API calls |
| `refreshToken` | string | JWT for token refresh (if implemented client-side) |

Side effect: updates `last_login` on the user row.

### Errors

| HTTP | Code | When |
|------|------|------|
| 401 | `error.auth.invalid_credentials` | Wrong username or password |

---

## POST `/auth/find-username`

Looks up account by email and emails the username to that address.

### Request

| Type | Name | Required |
|------|------|----------|
| Query | `email` | Yes |

```http
POST /auth/find-username?email=user@example.com
```

### Response

**200 OK** — empty body (email sent if account exists)

### Errors

| HTTP | Code | When |
|------|------|------|
| 404 | `error.auth.email_not_found` | No account for email |

---

## POST `/auth/find-password`

Generates a random 9-character temporary password, saves it (hashed), and emails it in plain text.

### Request

| Type | Name | Required |
|------|------|----------|
| Query | `email` | Yes |

```http
POST /auth/find-password?email=user@example.com
```

### Response

**200 OK** — empty body

### Errors

| HTTP | Code | When |
|------|------|------|
| 404 | `error.auth.email_not_found` | No account for email |

---

## Example — login

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"luna","password":"Secret123!"}'
```

Use `accessToken` on [Profile](./profile.md) endpoints:

```http
Authorization: Bearer {accessToken}
```
