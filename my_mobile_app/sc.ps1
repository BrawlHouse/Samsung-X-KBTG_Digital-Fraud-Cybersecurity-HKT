# --- PART 1: แก้ build.gradle (เผื่อไว้) ---
$gradlePath = "$env:LOCALAPPDATA\Pub\Cache\hosted\pub.dev\phone_state-1.0.4\android\build.gradle"
if (Test-Path $gradlePath) {
    # เขียนทับด้วย Config ที่ถูกต้อง (มี namespace)
    $gradleContent = @'
group 'it.mainella.phone_state'
version '1.0-SNAPSHOT'

buildscript {
    ext.kotlin_version = '1.6.10'
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.1.2'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

rootProject.allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'

android {
    namespace 'com.it_nomads.phone_state'
    compileSdkVersion 33
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }

    defaultConfig {
        minSdkVersion 16
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
}
'@
    Set-Content -Path $gradlePath -Value $gradleContent
    Write-Host "✅ Fixed build.gradle" -ForegroundColor Green
}

# --- PART 2: แก้ AndroidManifest.xml (ลบ package="..." ทิ้ง) ---
$manifestPath = "$env:LOCALAPPDATA\Pub\Cache\hosted\pub.dev\phone_state-1.0.4\android\src\main\AndroidManifest.xml"
if (Test-Path $manifestPath) {
    # เขียนทับด้วย Manifest ที่สะอาด (ไม่มี attribute package)
    $cleanManifest = @'
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application />
</manifest>
'@
    Set-Content -Path $manifestPath -Value $cleanManifest
    Write-Host "✅ Fixed AndroidManifest.xml (Removed package attribute)" -ForegroundColor Green
    
    # --- PART 3: รันเลย! ---
    Write-Host "🚀 Launching App..." -ForegroundColor Cyan
    flutter run
} else {
    Write-Host "❌ Error: ไม่เจอไฟล์ Plugin" -ForegroundColor Red
}