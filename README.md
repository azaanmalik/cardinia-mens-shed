# Cardinia Men's Shed

Android community management application for community events, attendance, member chat, marketplace listings and profiles.

[![Android CI](https://github.com/azaanmalik/cardinia-mens-shed/actions/workflows/android-ci.yml/badge.svg)](https://github.com/azaanmalik/cardinia-mens-shed/actions/workflows/android-ci.yml)

## Overview

A Java Android app integrating **Firebase Authentication**, **Cloud Firestore** and **Supabase Storage** to support a community membership experience.

**Features:** registration and login, password reset, member profiles, event discovery and booking, attendance, event chat, marketplace items and information pages.

## Stack

| Layer | Implementation |
| --- | --- |
| Android | Java, AndroidX, Material Design, ViewBinding |
| Build | Gradle 8.7, Java 17, Android Gradle Plugin |
| Android versions | compileSdk 35, targetSdk 34, minSdk 24 |
| Authentication | Firebase Auth |
| Data | Cloud Firestore and Firebase database dependencies |
| Images | Supabase Storage with Glide and Coil |

## Run from source

1. Clone this repository and open its root directory in **Android Studio**.
2. Install **JDK 17** and Android SDK Platform **35**, then sync Gradle.
3. Create your own Firebase project, register Android package `com.example.cardiniamensshed`, enable Email/Password Auth and configure Firestore access rules.
4. Download your Firebase Android configuration as `google-services.json` and save it locally at `app/google-services.json`. **It is excluded from Git.**
5. If using Supabase Storage, configure your own Supabase URL and **publishable/anon key** as `SUPABASE_URL` and `SUPABASE_ANON_KEY` in your **user-level** `~/.gradle/gradle.properties` (see `gradle-secrets.example.properties`). **Never put a service-role/secret key in an APK.**
6. Set up the `profile-pictures` and `event-images` buckets and a **secure authorization integration**. Firebase Auth does not automatically grant Supabase Storage permissions; a verified backend or signed-upload solution is needed. The original direct upload is not production-secure.
7. Run on an emulator or Android 7.0+ device.

Build and run unit tests:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Windows: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.

## Repository layout

```text
app/src/main/java/com/example/cardiniamensshed/  Java application code
app/src/main/res/                            Layouts, icons, themes and menus
app/src/test/                                Local unit tests
app/src/androidTest/                         Device tests
app/build.gradle.kts                         Android build and dependencies
gradle/wrapper/                              Gradle wrapper
.github/workflows/android-ci.yml             Build verification
SECURITY.md                                  Cloud privacy and safety guidance
gradle-secrets.example.properties            Local config example
```

## Build vs. functional verification

GitHub Actions will compile the debug APK and execute unit tests **after the source upload**. The CI build uses synthetic Firebase configuration for **build-only** checks: it is not a running cloud backend. Firebase features require your own project, rules and credentials; storage uploads require secure authorization design. Passing CI does not confirm cloud integration or emulator UX.

## Security notice

**The original development ZIP contained a hardcoded Supabase `service_role` key. Revoke/rotate this key in Supabase immediately.** This repository's clean source package omits it, excludes the original Firebase configuration and removes local SDK settings and build artifacts. See [SECURITY.md](SECURITY.md).

## License

[MIT](LICENSE) applies to original software where contributors have the rights to grant it. Third-party content and assets keep their own terms.
