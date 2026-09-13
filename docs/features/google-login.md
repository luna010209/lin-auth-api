# Google login (LinAuth)

Google sign-in for **lin-auth-api**. Clients (Flutter app) sign in with Google via Firebase, then call LinAuth with a Firebase ID token.

API details: [firebase-app.md](firebase-app.md)

---

## Architecture

```text
Client → Google sign-in (Firebase Auth) → getIdToken()
  → POST /auth/app/*     Authorization: Bearer {firebaseIdToken}
  → Protected APIs       Authorization: Bearer {firebaseIdToken} or JWT
```

**Filter chain (protected routes):**

```text
FirebaseAuthFilter  → Firebase token → verify → set auth (if user in DB)
JwtFilter           → Firebase token → skip; JWT → verify
```

Both token types use the same `Authorization: Bearer` header.

---

## Firebase / Google setup

1. [Firebase Console](https://console.firebase.google.com/) → same project as Flutter app
2. **Authentication** → **Sign-in method** → **Google** → Enable
3. [Google Cloud Console](https://console.cloud.google.com/) → **OAuth consent screen** → configure + test users (or publish)
4. **Project settings** → **Service accounts** → **Generate new private key**
5. Set in `.env.local.properties`:

```properties
FIREBASE_JSON=/absolute/path/to/firebase-service-account.json
PROJECT_ID=lin-universe
```

6. Start LinAuth: `./gradlew bootRun` (no Firebase init errors = OK)

**Web (`lin-langa-web`):** add to `.env`:

```properties
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=lin-universe.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=lin-universe
VITE_FIREBASE_STORAGE_BUCKET=lin-universe.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=...
VITE_FIREBASE_APP_ID=...
```

Login page shows **Continue with Google** when these are set.

**Skip:** Firebase Hosting.

---

## What Google provides (via Firebase token)

| From token | Maps to `auth` | User must fill |
|------------|----------------|----------------|
| `uid` | `firebase_uid` | — |
| `email` | `email` | — |
| `name` | `display_name` (pre-fill) | `username` |
| `picture` | optional | `password` |
| `email_verified` | trusted (no email_verification table) | `phone` (optional) |

---

## Client flow

```text
1. Google sign-in → idToken = await user.getIdToken()

2. POST /auth/app
   Authorization: Bearer {idToken}

3. Handle status:
   LOGIN_SUCCESS  → JWT returned → logged in
   NEED_REGISTER  → dialog on login page → POST /auth/app/register

4. Login page dialog → POST /auth/app/register
   { "username", "password", "cfPassword", "displayName?", "phone?" }
   (email + uid from token; no email_verification table — Google email_verified is trusted)
```

### `POST /auth/app` server logic

1. `firebase_uid` in DB → `LOGIN_SUCCESS`
2. Email in DB, uid empty → set `firebase_uid` from token → `LOGIN_SUCCESS` (auto-link)
3. Email in DB, different uid → `409 error.auth.firebase_already_linked`
4. No match → `NEED_REGISTER`

Password users signing in with Google for the first time are auto-linked by verified Google email — no username/password link step.

### `POST /auth/app/register`

- Creates user with `firebase_uid`, username, password, phone from form
- Requires `email_verified=true` on Firebase token
- Does **not** use the `email_verification` table

### Removed (2026-09-13)

- `EMAIL_EXIST` status
- `POST /auth/app/confirm`
- `POST /auth/app/verify-email`
- Web link dialog (`GoogleLinkDialog`)

### NEED_REGISTER response example

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

---

## curl test

```bash
curl -X POST http://localhost:8081/auth/app \
  -H "Authorization: Bearer FIREBASE_ID_TOKEN"

curl http://localhost:8081/auth \
  -H "Authorization: Bearer FIREBASE_ID_TOKEN"
```

---

## Checklist

- [ ] Google enabled in Firebase Authentication
- [ ] OAuth consent screen configured
- [ ] Service account JSON + `FIREBASE_JSON` + `PROJECT_ID` set
- [ ] LinAuth starts without Firebase errors
- [ ] App sends Bearer token on all `/auth/app/*` calls

---

## Common errors

| Symptom | Fix |
|---------|-----|
| Firebase init fails | Check `FIREBASE_JSON` path |
| `error.token.missing` | Send `Authorization: Bearer {idToken}` |
| `error.token.invalid` | Wrong `PROJECT_ID`, expired token, or wrong Firebase project |
| Google sign-in fails in app | OAuth consent screen / test users / provider not enabled |
| `error.auth.firebase_already_linked` | Email linked to a different Firebase UID |
