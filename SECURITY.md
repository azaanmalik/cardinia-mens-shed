# Security and privacy

**Immediate action required:** The provided original development archive contained a hard-coded Supabase **service-role key** in `SupabaseUploader.java`. Service-role credentials can bypass row-level security. **Revoke/rotate that key in the original Supabase project immediately**, and never publish the original ZIP. The professional upload package omits the key.

- The original Firebase `app/google-services.json` is not included; each developer should configure their own project. Project-specific Firebase configuration is not necessarily a secret, but should not make forks share the original backend.
- Supabase `SUPABASE_URL` and **anon/publishable** keys belong in a private developer configuration. Supabase **service-role/secret keys must never be embedded in client code, Android resources, Gradle commits or an APK**.
- Firebase-authenticated users are not automatically Supabase Auth users. Direct client storage operations require a properly secured authorization design, e.g., a backend verifying Firebase ID tokens and issuing scoped upload URLs. Do not enable public unrestricted writes to make uploads function.
- Restrict Firebase Firestore / Realtime Database rules per user or membership role, validate event data, and prevent unauthorized access to chat, attendance and profile records.
- Never commit real member names, contact details, photos, account records, tokens, signing keys or local database backups.
- Android backups are disabled in the prepared source. Conduct a full security and privacy review before real-world use.

To report a security issue, contact the repository maintainer privately.
