plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "id.pengingat.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "id.pengingat.app"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
    }

    // Signing konsisten untuk CI: bila GitHub Secrets keystore tersedia
    // (properti CI_STORE_FILE dkk diteruskan workflow), pakai itu.
    // Bila tidak, release jatuh ke debug signing (tetap bisa diinstal,
    // tapi antar-build lokal/CI tanda tangannya bisa berbeda).
    val ciStoreFile = (findProperty("CI_STORE_FILE") as String?)?.takeIf { it.isNotBlank() }
    signingConfigs {
        if (ciStoreFile != null) {
            create("ci") {
                storeFile = file(ciStoreFile)
                storePassword = findProperty("CI_STORE_PASSWORD") as String? ?: ""
                keyAlias = findProperty("CI_KEY_ALIAS") as String? ?: ""
                keyPassword = findProperty("CI_KEY_PASSWORD") as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (ciStoreFile != null) signingConfigs.getByName("ci")
                            else signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Data lokal
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Widget
    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")

    // Penjadwalan cadangan
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Root (libsu)
    implementation("com.github.topjohnwu.libsu:core:5.2.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
