import os

gradle_path = "app/build.gradle.kts"

# ใส่ buildToolsVersion = "34.0.0" เข้าไปแบบระบุเจาะจง
fixed_content = """plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.20"
}

android {
    namespace = "com.riskguard.frontend"
    compileSdk = 34
    buildToolsVersion = "34.0.0"  // <--- บรรทัดนี้คือพระเอกของเรา

    defaultConfig {
        applicationId = "com.riskguard.frontend"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
"""

with open(gradle_path, "w") as f:
    f.write(fixed_content)

print("✅ Forced Build Tools to version 34.0.0!")