// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false



}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.0") // Use your desired version of the Android Gradle plugin
        classpath("com.google.gms:google-services:4.4.2") // For the Google Services plugin
    }
}


