plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")

}

android {
    namespace = "com.example.cardiniamensshed"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cardiniamensshed"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val supabaseUrl = providers.gradleProperty("SUPABASE_URL").orNull ?: ""
        val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY").orNull ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation ("androidx.core:core:1.6.0")
    // Android Core
    implementation("androidx.core:core-ktx:1.6.0")
    implementation(libs.constraintlayout)
    implementation(libs.appcompat)
    implementation(libs.material)


    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.cast.framework)

    // Testing
    implementation(libs.junit)
    implementation(libs.activity)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
//google services//
    implementation ("com.google.android.gms:play-services-auth:20.6.0")
//for sweetalerts//
    implementation ("com.github.f0ris.sweetalert:library:1.6.2")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    // Firebase
    implementation ("com.google.firebase:firebase-database:20.0.3") // Firebase Realtime Database
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")


    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:2.3.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:supabase-kt:1.2.3") // Use latest version
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.2.3")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.2.3")

    // File path utilities
    implementation("androidx.documentfile:documentfile:1.1.0")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("io.coil-kt:coil:2.5.0")
    implementation("com.github.dhaval2404:imagepicker:2.1")

    // Networking
    implementation("io.ktor:ktor-client-core:2.3.10")
    implementation("io.ktor:ktor-client-okhttp:2.3.8")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")

    // Kotlin & Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Retrofit (optional)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation ("androidx.appcompat:appcompat:1.3.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")  // For image upload

    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("com.google.android.material:material:1.11.0") // Or latest

}
