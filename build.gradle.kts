// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    dependencies {
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath ("com.android.tools.build:gradle:8.5.2")
        classpath("com.google.gms:google-services:4.4.0")
    }
    }


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}