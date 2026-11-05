# Publish all source files safely

The GitHub scaffold is committed through the connected GitHub integration. The cleaned ZIP contains all intended Android source, resources, Gradle wrapper files and documentation, with original Gradle outputs and **both original cloud configurations removed**.

1. Clone `https://github.com/azaanmalik/cardinia-mens-shed.git` using GitHub Desktop.
2. Extract **the ZIP contents** into the cloned repo root. Root must contain `app/`, `gradle/`, `gradlew`, `README.md`, `.github/`, and `LICENSE`; do not create an extra `CardiniaMensShed/` folder.
3. Commit and push all source and resources to `main`.
4. Check the **latest** GitHub Actions run. A pre-upload green workflow only means the repository scaffold was valid; real Android compilation runs **after the source upload**.
5. Supply your own `app/google-services.json` and `SUPABASE_URL`/`SUPABASE_ANON_KEY` privately for local development. Never upload a Supabase service-role key.

**Critical:** Rotate/revoke the service-role key from the original archive before any deployment or reuse. It must not be committed to GitHub, even in git history.
