plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.chatapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chatapplication"
        minSdk = 26
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.material)
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    //zego
    implementation("com.github.ZEGOCLOUD:zego_inapp_chat_uikit_android:+")
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation("com.guolindev.permissionx:permissionx:1.8.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.4.0")


    //supabase
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.1")

    //ktor
    implementation("io.ktor:ktor-client-android:2.3.13")
    implementation("io.ktor:ktor-client-core: 2.3.13")
    implementation("io.ktor:ktor-utils: 2.3.13")

    //material
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
}