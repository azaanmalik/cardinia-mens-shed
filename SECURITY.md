# Security and configuration

**Important incident:** The originally provided development archive contained a Supabase `service_role` key hard-coded inside `SupabaseUploader.java`, plus a publishable/anon key in `gradle.properties`. The service-role key can bypass Row Level Security. **Revoke / rotate the service-role key in Supabase immediately**, even though it has not been included in this repository. Do not publish the original archive.

- `app/google-services.json` is intentionally excluded. Developers must supply their own Firebase Android configuration.
- A Firebase API key in `google-services.json` is not normally a server-side secret, but the original project-specific configuration is intentionally omitted to avoid coupling forks to someone else's Firebase project.
- Set only `SUPABASE_URL` and a **publishable/anon** key in your user `~/.gradle/gradle.properties`. Never embed a Supabase `service_role` / secret key in a client APK or source.
- **Firebase Auth is not Supabase Auth.** A Firebase-authenticated user's ID token does not automatically authorize Supabase Storage RLS. Use a verified backend / server-side upload token service and strong per-user policies for sensitive files. The legacy client-side uploader is preserved for learning, but is not verified for secure production upload.
- Apply strict Firebase Firestore and Realtime Database rules, turn on necessary auth providers, configure App Check where applicable, and test with separate user accounts.
- Disable untrusted device backups for membership data; the manifest sets `allowBackup=false`.
- Do not publish member data, profiles, real event records, uploaded files, private keys, signed APKs, or storage service credentials.
- Report vulnerabilities privately to the maintainer.
