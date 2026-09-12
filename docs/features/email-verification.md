# Email verification

Send and confirm a 6-digit code before web sign-up or app registration (when not using social email trust).

**Package:** `io.lin.auth.feature.emailverification`  
**Auth:** Public (no JWT)

---

## POST `/auth/email-verification/send-mail`

Sends a verification code to the email. Code expires in **20 minutes**.

### Request

| Type | Name | Required | Validation |
|------|------|----------|------------|
| Query | `email` | Yes | Valid email format |

```http
POST /auth/email-verification/send-mail?email=user@example.com
```

### Response

**200 OK** — empty body

Also sends an HTML email containing the 6-digit code.

### Errors

| HTTP | Code | When |
|------|------|------|
| 409 | `error.auth.email_conflict` | Email already registered |
| 400 | `valid.email` / `valid.email_format` | Missing or invalid email |

---

## POST `/auth/email-verification/verify`

Confirms the code sent to the email. After success, the email can be used for sign-up.

### Request

**Content-Type:** `application/json`

```json
{
  "email": "user@example.com",
  "verifiedCode": "123456"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `email` | string | Yes | Normalized to lowercase |
| `verifiedCode` | string | Yes | 6-digit code from email |

### Response

**200 OK** — empty body

### Errors

| HTTP | Code | When |
|------|------|------|
| 409 | `error.auth.email_conflict` | Email already registered |
| 409 | `error.auth.email_already_verified` | Already verified |
| 408 | `error.auth.verification.expired` | Code expired |
| 409 | `error.auth.verification.invalid_code` | Wrong code |
| 400 | `error.auth.verification.not_sent` | No verification row for email |

---

## Typical flow

```text
1. POST /auth/email-verification/send-mail?email=...
2. POST /auth/email-verification/verify  { email, verifiedCode }
3. POST /auth/sign-up  (or /auth/app/register)
```
