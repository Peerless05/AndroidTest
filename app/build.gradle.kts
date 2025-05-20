plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.madproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.madproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    //implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.17")
    implementation("com.facebook.android:facebook-android-sdk:11.0.0")
    implementation("com.facebook.android:facebook-login:latest.release")
    implementation ("com.google.android.gms:play-services-auth:21.1.0")
    implementation ("com.google.gms:google-services:4.4.2")
    implementation ("com.google.firebase:firebase-auth:21.0.8")
    implementation ("com.google.firebase:firebase-firestore:24.7.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}