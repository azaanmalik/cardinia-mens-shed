<p align="center"><img src="docs/assets/banner.svg" alt="Cardinia Men's Shed community app" width="100%"></p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-Java-2C5F95?style=flat-square&amp;logo=android&amp;logoColor=white">
  <img alt="Firebase" src="https://img.shields.io/badge/Firebase-Authentication%20%26%20Firestore-E9B96E?style=flat-square&amp;logo=firebase&amp;logoColor=black">
  <img alt="Firebase" src="https://img.shields.io/badge/Firebase-Storage-3B8A75?style=flat-square&amp;logo=supabase&amp;logoColor=white">
  <a href="https://github.com/azaanmalik/cardinia-mens-shed/actions/workflows/android-ci.yml"><img alt="Android CI" src="https://github.com/azaanmalik/cardinia-mens-shed/actions/workflows/android-ci.yml/badge.svg"></a>
  <a href="LICENSE"><img alt="License MIT" src="https://img.shields.io/badge/License-MIT-142B52?style=flat-square"></a>
</p>

> **An Android community hub for members to connect, discover events, manage attendance, chat and share marketplace listings.**

— Community Android App

Android community-management application built with **Java**, **Firebase Authentication**, **Cloud Firestore**, and **Firebase Storage**. Designed to support community engagement, events, attendance, chat, member profiles, and a member marketplace.

> Portfolio project. This repository provides source code and setup guidance, but **does not include production cloud credentials, real member records, Firebase configuration or database rules**. Backend-dependent functions require separate configuration and access controls.

## Key modules

- Member registration, sign-in and password reset through Firebase Auth
- Dashboard and member profile with photo upload
- Community events: create, browse, join and book events
- Attendance records and participation tracking
- Event chat and member interaction
- Community marketplace and item listings
- Information / website WebView

## Technology

| Layer | Technology |
| --- | --- |
| Android | Java, AndroidX, Material Components, ViewBinding |
| Build | Gradle 8.7, Android Gradle Plugin 8.x, Java 17 |
| SDK | compileSdk 35, targetSdk 34, minSdk 24 |
| Identity | Firebase Authentication |
| Cloud data | Cloud Firestore and Firebase Realtime Database libraries |
| Images | Firebase Storage, Glide, Coil |

## Run with your own cloud project

1. Clone this repository and open the **root** (containing `settings.gradle.kts`) in Android Studio.
2. Install **JDK 17** and Android SDK **Platform 35**, including required build tools, then sync Gradle.
3. Create a Firebase project and register an Android app with package ID `com.example.cardiniamensshed`. Enable Email/Password authentication and set up Cloud Firestore. Obtain your own `google-services.json` from Firebase Console and place it at **`app/google-services.json`**; it is gitignored.
4. Create a Firebase project and appropriate Storage buckets named `profile-pictures` and `event-images` (if you plan to use image upload). Add **public URL** and **anon/publishable client key**, *never* a `service_role`/secret key, to your **user-level** `~/.gradle/gradle.properties` (see `gradle-secrets.example.properties`).
5. **Security requirement:** Firebase Auth identity is not automatically recognized by Firebase Auth or Firebase RLS. Direct Firebase Storage uploads require a correctly designed, restricted authorization solution (for example, a backend that verifies Firebase ID tokens and issues time-limited signed upload URLs). The original direct-upload architecture is **not production secure**; do not open public write access merely to make it work. Profile/event images may not upload until a secure integration is added.
6. In Firebase, configure Firestore/Realtime Database **security rules and required indexes** for your own application. Do not use unrestricted public read/write rules.
7. Run the `app` configuration on Android 7.0 (API 24) or newer. For a terminal build, run `./gradlew :app:assembleDebug :app:testDebugUnitTest` (Windows: `gradlew.bat`).

## Build / CI details

GitHub Actions validates the Android debug APK build and runs local unit tests. To allow a **build-only** validation without accessing anyone's Firebase project, CI generates a **synthetic** `google-services.json` compatible with the application package. Synthetic config is **not a functioning backend** and cannot be used for Firebase authentication or messaging. Real Firebase configuration must be supplied privately by each developer.

A successful CI build verifies compilation, **not** end-to-end sign-in, secure cloud storage permissions, Firebase integration, paid orders, or emulator UI behavior.

## Privacy / security

- No real user data, service-role secrets, production Firebase config or generated APKs are included.
- This archive originally contained a hardcoded **Firebase service-role key**. **Rotate/revoke that key immediately** in the original Firebase project. The uploaded GitHub package replaces the key with non-secret client configuration and excludes original credentials.
- The Android app is not suitable for storing actual sensitive membership information without backend access-control, privacy-policy and storage authorization reviews.
- See [SECURITY.md](SECURITY.md) for security caveats.

## License

See [LICENSE](LICENSE). The MIT license applies to original code only where contributors hold appropriate rights; third-party libraries, images and content retain their own licenses.
