# Email logo: CID inline → configurable URL

## Summary

Replaced `cid:logoImage` inline attachments with a public HTTPS URL from `app.mail.logo-url` (`LOGO_URL` env).

## Reason

- Spring `MimeMessageHelper` inline images were added after `setText()`, so CID references did not resolve.
- Resend preview and many clients render hosted URLs more reliably than CID attachments.
- Removes ~739 KB embedded image from every outbound message.

## Implementation

- `application-local.yaml`: `app.mail.logo-url: ${LOGO_URL}`
- Mail listeners read `@Value("${app.mail.logo-url}")` and set `<img src="...">` in HTML templates.
- Removed `addInline()` and classpath `logo.png` attachment from all three listeners.

## Affected files

- `EmailVerifyListenerEvent.java`
- `UsernameListenerEvent.java`
- `PasswordListenerEvent.java`
- `application-local.yaml`
- `.env.local.example`

## Verification

1. Set `LOGO_URL` to a publicly accessible HTTPS image URL.
2. Restart lin-auth-api.
3. Send verification email; confirm logo in Resend preview and recipient inbox.
