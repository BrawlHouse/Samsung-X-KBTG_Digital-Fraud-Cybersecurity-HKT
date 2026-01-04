import os

# สร้างโฟลเดอร์เผื่อไว้ (ปกติมีอยู่แล้ว)
os.makedirs("gradle/wrapper", exist_ok=True)

# เขียนค่า Config ใหม่ บังคับใช้ Gradle 8.7 แทน 9.2.1
properties_content = """distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"""

with open("gradle/wrapper/gradle-wrapper.properties", "w") as f:
    f.write(properties_content)

print("✅ Downgraded Gradle to 8.7 (Stable) successfully!")