import os

# ================= CONFIG =================
PACKAGE = "com.riskguard.frontend"
PACKAGE_ROOT = "app/src/main/java/com/riskguard/frontend"
RES_ROOT = "app/src/main/res"
MANIFEST_PATH = "app/src/main/AndroidManifest.xml"
GRADLE_PATH = "app/build.gradle.kts"

# ================= build.gradle =================
BUILD_GRADLE = """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.riskguard.frontend"
    compileSdk = 34

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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
"""

# ================= Manifest =================
MANIFEST = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>

    <application
        android:label="RiskGuard"
        android:icon="@android:drawable/btn_star"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <activity
            android:name=".ui.OverlayChatActivity"
            android:launchMode="singleTask"
            android:excludeFromRecents="true"
            android:theme="@style/Theme.AppCompat.Light.Dialog" 
            android:exported="false"/>

        <service
            android:name=".monitor.CallMonitorService"
            android:exported="false"
            android:foregroundServiceType="phoneCall"/>
    </application>
</manifest>
"""

# ================= MainActivity =================
MAIN_ACTIVITY = """package com.riskguard.frontend.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.riskguard.frontend.R
import com.riskguard.frontend.monitor.CallMonitorService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)
            ) {
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ), 100
                )
            } else {
                startSystem()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && Settings.canDrawOverlays(this)) {
            startSystem()
        }
    }

    private fun startSystem() {
        CallMonitorService.start(this)
    }
}
"""

# ================= Service =================
SERVICE = """package com.riskguard.frontend.monitor

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.riskguard.frontend.inject.BankInjectSimulator

class CallMonitorService : Service() {

    override fun onCreate() {
        super.onCreate()
        val notification: Notification = NotificationCompat.Builder(this, "risk")
            .setContentTitle("RiskGuard")
            .setContentText("Monitoring active")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Handler(Looper.getMainLooper()).postDelayed({
            BankInjectSimulator.trigger(this)
        }, 5000)

        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, CallMonitorService::class.java)
            ctx.startForegroundService(i)
        }
    }
}
"""

# ================= Simulator =================
SIMULATOR = """package com.riskguard.frontend.inject

import android.content.Context
import android.content.Intent
import com.riskguard.frontend.ui.OverlayChatActivity

object BankInjectSimulator {

    fun trigger(ctx: Context) {
        val i = Intent(ctx, OverlayChatActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        ctx.startActivity(i)
    }
}
"""

# ================= Overlay Activity =================
OVERLAY = """package com.riskguard.frontend.ui

import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.riskguard.frontend.R

class OverlayChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_overlay)

        findViewById<TextView>(R.id.tv).text =
            "⚠️ พบพฤติกรรมเสี่ยงจากการสนทนา\nกรุณาหยุดและติดต่อครอบครัว"

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }
}
"""

# ================= Layouts =================
LAYOUT_MAIN = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:id="@+id/btnStart"
        android:text="Start RiskGuard"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>
</LinearLayout>
"""

LAYOUT_OVERLAY = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:background="#CC000000"
    android:padding="24dp"
    android:gravity="center"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/tv"
        android:textColor="#FFFFFF"
        android:textSize="20sp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>

    <Button
        android:id="@+id/btnClose"
        android:text="ปิด"
        android:layout_marginTop="24dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
</LinearLayout>
"""

# ================= Writer =================
def write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

def main():
    write(GRADLE_PATH, BUILD_GRADLE)
    write(MANIFEST_PATH, MANIFEST)

    write(f"{PACKAGE_ROOT}/ui/MainActivity.kt", MAIN_ACTIVITY)
    write(f"{PACKAGE_ROOT}/ui/OverlayChatActivity.kt", OVERLAY)
    write(f"{PACKAGE_ROOT}/monitor/CallMonitorService.kt", SERVICE)
    write(f"{PACKAGE_ROOT}/inject/BankInjectSimulator.kt", SIMULATOR)

    write(f"{RES_ROOT}/layout/activity_main.xml", LAYOUT_MAIN)
    write(f"{RES_ROOT}/layout/activity_overlay.xml", LAYOUT_OVERLAY)

    print("✅ RiskGuard project generated (READY TO RUN)")

if __name__ == "__main__":
    main()
