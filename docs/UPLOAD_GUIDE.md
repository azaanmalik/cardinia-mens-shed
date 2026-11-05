# Completing the GitHub upload

The GitHub scaffold (README, license, security guidance and CI) is committed directly through the linked GitHub integration. The complete safe Android source package is provided as `Cardinia-Mens-Shed-GitHub-Upload.zip`.

1. Clone `https://github.com/azaanmalik/cardinia-mens-shed.git` in GitHub Desktop.
2. Extract **all ZIP contents**, not the ZIP file itself, into the cloned repository root. The root should contain `app/`, `gradle/`, `gradlew`, `gradlew.bat`, `README.md` and `LICENSE`, without an additional `CardiniaMensShed/` parent folder.
3. Review the changes, commit and **Push origin** to `main`.
4. Check the latest GitHub Actions build. Only the build **after source upload** actually compiles the Android app.
5. For local runtime, supply your **own** Firebase `app/google-services.json`, and optional Supabase URL and anon key. You must implement/verify secure storage authorization separately.

Generated Gradle caches, APK outputs, machine-specific `local.properties`, the original Firebase configuration and original Supabase secret keys are intentionally excluded.

**Rotate/revoke the service-role key present in the original uploaded ZIP before deploying or sharing that original archive.**
