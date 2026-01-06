import os

print("Starting Emergency Fix for Error 25.0.1...")

# 1. เช็คว่ามีไฟล์ Gradle ผี (Groovy) ซ่อนอยู่ไหม
ghost_file = "app/build.gradle"
if os.path.exists(ghost_file):
    print(f"Found ghost file '{ghost_file}'. Deleting it...")
    os.remove(ghost_file)
else:
    print("No ghost build.gradle file found.")

# 2. เช็คว่า local.properties ชี้ไปถูกที่ไหม
if os.path.exists("local.properties"):
    with open("local.properties", "r") as f:
        content = f.read()
        print(f"SDK Location config:\n{content.strip()}")
else:
    print("ERROR: local.properties is missing!")

# 3. เขียนไฟล์ app/build.gradle.kts ใหม่ (บังคับใช้ 34.0.0)
target_file = "app/build.gradle.kts"
new_content = """plugins {
    id("com.android.application") version "8.2.0"
    id("org.jetbrains.kotlin.android") version "1.9.20"
}

android {
    namespace = "com.riskguard.frontend"
    compileSdk = 34
    
    // Forced buildToolsVersion to fix error 25.0.1
    buildToolsVersion = "34.0.0"

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

# แก้ตรงนี้: เพิ่ม encoding="utf-8" เพื่อรองรับทุกภาษา
with open(target_file, "w", encoding="utf-8") as f:
    f.write(new_content)

print(f"Forced 'buildToolsVersion = \"34.0.0\"' into {target_file}")
print("Ready to build!")