# Profile

Authenticated user profile: read info, update details, change password, upload avatar.

**Package:** `io.lin.auth.feature.profile`  
**Auth:** **Required** — `Authorization: Bearer {accessToken}`

---

## GET `/auth`

Returns the currently logged-in user.

### Request

No body.

```http
GET /auth
Authorization: Bearer {accessToken}
```

### Response

**200 OK** — [UserInfo](./account.md#userinfo)

```json
{
  "id": 1,
  "username": "luna",
  "displayName": "Luna",
  "email": "user@example.com",
  "phone": "01012345678",
  "uid": null,
  "avatar": "https://cdn.example.com/profiles/1/avatar.jpg",
  "lastLogin": "2026-09-12T10:30:00",
  "createdAt": "2026-09-12T09:00:00",
  "roles": ["ROLE_TEACHER"]
}
```

`avatar` is a public CDN URL when set; otherwise `null`.

### Errors

| HTTP | Code | When |
|------|------|------|
| 401 | `error.auth.login_required` | Missing or invalid JWT |

---

## PUT `/auth`

Update username, display name, phone, or email.

### Request

**Content-Type:** `application/json`

```json
{
  "username": "luna_new",
  "email": "new@example.com",
  "displayName": "Luna Kim",
  "phone": "01099998888"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `username` | string | Yes | Must be unique if changed |
| `email` | string | Yes | New email must be [verified](./email-verification.md) first |
| `displayName` | string | No | |
| `phone` | string | No | |

Changing email clears `firebase_uid` if it was set.

### Response

**200 OK** — empty body

### Errors

| HTTP | Code | When |
|------|------|------|
| 401 | `error.auth.login_required` | Not logged in |
| 409 | `error.auth.username_conflict` | Username taken |
| 409 | `error.auth.email_conflict` | Email taken |
| 400 | `error.auth.email.not_verified` | New email not verified |

---

## PUT `/auth/change-password`

Change password for the logged-in user.

### Request

**Content-Type:** `application/json`

```json
{
  "currentPassword": "OldSecret123!",
  "newPassword": "NewSecret456!",
  "cfPassword": "NewSecret456!"
}
```

| Field | Type | Required |
|-------|------|----------|
| `currentPassword` | string | Yes (checked in service) |
| `newPassword` | string | Yes |
| `cfPassword` | string | Yes |

### Response

**200 OK** — empty body

### Errors

| HTTP | Code | When |
|------|------|------|
| 401 | `error.auth.login_required` | Not logged in |
| 409 | `error.auth.current_password_invalid` | Wrong current password |
| 409 | `error.auth.password.mismatch` | New passwords differ |

---

## PUT `/auth/avatar`

Upload profile image (multipart).

### Request

**Content-Type:** `multipart/form-data`

| Part | Type | Required | Constraints |
|------|------|----------|-------------|
| `avatar` | file | Yes | JPEG, PNG, or WEBP; max **2 MB** |

```http
PUT /auth/avatar
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

avatar=@photo.jpg
```

Stored at R2 key: `profiles/{userId}/avatar.jpg`

### Response

**200 OK** — empty body

### Errors

| HTTP | Code | When |
|------|------|------|
| 401 | `error.auth.login_required` | Not logged in |
| 400 | `error.file.too_large` | File > 2 MB |
| 400 | `error.image.invalid_type` | Unsupported MIME type |
| 500 | `error.file.upload_failed` | Storage error |

---

## Example

```bash
# Get profile
curl http://localhost:8081/auth \
  -H "Authorization: Bearer {accessToken}"

# Upload avatar
curl -X PUT http://localhost:8081/auth/avatar \
  -H "Authorization: Bearer {accessToken}" \
  -F "avatar=@photo.jpg"
```
