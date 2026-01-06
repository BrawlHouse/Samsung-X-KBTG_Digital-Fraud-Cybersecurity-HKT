import os

# 1. สร้าง settings.gradle.kts (บอก Gradle ว่ามี app module)
settings_content = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "RiskGuard"
include(":app")
"""

with open("settings.gradle.kts", "w") as f:
    f.write(settings_content)
print("✅ Created settings.gradle.kts")

# 2. สร้าง local.properties (บอกที่อยู่ SDK)
# พยายามหา Path อัตโนมัติจาก Environment
sdk_path = os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT')
if not sdk_path:
    # ถ้าหาไม่เจอ ให้เดา Path มาตรฐานของ Windows
    sdk_path = os.path.join(os.environ['LOCALAPPDATA'], 'Android', 'Sdk')

# แปลง \ เป็น / กัน error
sdk_path = sdk_path.replace('\\', '/')

with open("local.properties", "w") as f:
    f.write(f"sdk.dir={sdk_path}")
print(f"✅ Created local.properties (SDK: {sdk_path})")

print("\n🔧 Repair Complete! Try running the build command again.")



# .\gradlew :app:installDebug