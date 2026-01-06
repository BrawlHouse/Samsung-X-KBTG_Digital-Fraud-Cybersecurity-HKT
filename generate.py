import os

# --- Configuration ---
PACKAGE_ROOT = "app/src/main/java/com/riskguard/frontend"
RES_ROOT = "app/src/main/res"
MANIFEST_PATH = "app/src/main/AndroidManifest.xml"
GRADLE_PATH = "app/build.gradle.kts"

# --- Content Templates ---

BUILD_GRADLE_CONTENT = """plugins {
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

MANIFEST_CONTENT = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

    <application
        android:name=".App"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="RiskGuard"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar"
        tools:targetApi="31">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".ui.OverlayChatActivity"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar"
            android:launchMode="singleInstance"
            android:excludeFromRecents="true" 
            android:exported="false"/>

        <service android:name=".monitor.CallMonitorService"
            android:foregroundServiceType="phoneCall"
            android:exported="false" />

    </application>

</manifest>
"""

# --- Kotlin Files Content ---

FILE_APP = """package com.riskguard.frontend

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.riskguard.frontend.monitor.CallMonitorService

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // เริ่ม Service ทันทีที่เปิดแอป (ในของจริงควรเริ่มหลังขอ Permission)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "risk_service",
                "Risk Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
"""

FILE_CONFIG = """package com.riskguard.frontend.config

object AppConfig {
    // True = ใช้ Mock Data ทั้งหมด (Phase 1-2)
    // False = ต่อ Node.js จริง (Phase 3+)
    const val IS_MOCK_MODE = true 
}
"""

FILE_EVENT_BUS = """package com.riskguard.frontend.util

import com.riskguard.frontend.model.CallSession

// EventBus แบบบ้านๆ สำหรับสื่อสารระหว่าง Service กับ Simulator
object EventBus {
    private var listener: ((CallSession) -> Unit)? = null

    fun register(l: (CallSession) -> Unit) {
        listener = l
    }

    fun emit(session: CallSession) {
        listener?.invoke(session)
    }
}
"""

FILE_MODELS = """package com.riskguard.frontend.model

import java.io.Serializable

data class CallSession(
    val phoneNumber: String,
    val durationSec: Long
) : Serializable

data class RiskResult(
    val score: Int,
    val level: String,
    val summary: String
)
"""

FILE_MOCK_API = """package com.riskguard.frontend.network

import android.os.Handler
import android.os.Looper
import com.riskguard.frontend.model.RiskResult

object MockApi {
    fun evaluateRisk(callback: (RiskResult) -> Unit) {
        // Mock: รอ 1.5 วิ แล้วตอบกลับว่าเสี่ยงสูง
        Handler(Looper.getMainLooper()).postDelayed({
            callback(
                RiskResult(
                    score = 90,
                    level = "HIGH",
                    summary = "!! เตือนภัย !!\\nปลายสายมีการเร่งรัดให้โอนเงิน\\nและคุยนานผิดปกติ"
                )
            )
        }, 1500)
    }
}
"""

FILE_API_CLIENT = """package com.riskguard.frontend.network

import com.riskguard.frontend.config.AppConfig
import com.riskguard.frontend.model.RiskResult

object ApiClient {
    fun checkRisk(callback: (RiskResult) -> Unit) {
        if (AppConfig.IS_MOCK_MODE) {
            MockApi.evaluateRisk(callback)
        } else {
            // TODO: Connect Node.js here
        }
    }
}
"""

FILE_INJECT_SIM = """package com.riskguard.frontend.inject

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.riskguard.frontend.model.CallSession
import com.riskguard.frontend.ui.OverlayChatActivity

object BankInjectSimulator {

    fun waitAndTrigger(ctx: Context, session: CallSession) {
        // Mock Scenario:
        // Service จับได้ว่าคุยนาน -> ส่งมานี่
        // นี่คือ Simulator แกล้งทำเป็นว่า User กำลังจะกดแอป Bank ในอีก 2 วิ
        
        println("SIMULATOR: User is talking... Waiting for Bank App launch...")
        
        Handler(Looper.getMainLooper()).postDelayed({
            println("SIMULATOR: Bank App Launched! -> TRIGGERING OVERLAY")
            launchOverlay(ctx, session)
        }, 2000)
    }

    private fun launchOverlay(ctx: Context, session: CallSession) {
        val i = Intent(ctx, OverlayChatActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // i.putExtra("session", session) 
        ctx.startActivity(i)
    }
}
"""

FILE_MONITOR_SERVICE = """package com.riskguard.frontend.monitor

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.riskguard.frontend.R
import com.riskguard.frontend.inject.BankInjectSimulator
import com.riskguard.frontend.model.CallSession

class CallMonitorService : Service() {

    override fun onCreate() {
        super.onCreate()
        // ต้องมี Notification สำหรับ Foreground Service
        val notification: Notification = NotificationCompat.Builder(this, "risk_service")
            .setContentTitle("RiskGuard Active")
            .setContentText("Monitoring calls for safety...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("SERVICE: Started Monitoring...")

        // MOCK LOGIC: 
        // แกล้งทำเป็นว่า อีก 5 วินาที จะมีการโทรครบ 10 นาที
        Handler(Looper.getMainLooper()).postDelayed({
            val mockSession = CallSession("089-999-9999", 600)
            println("SERVICE: Detected Call > 10 mins")
            
            // ส่งต่อให้ Simulator ทำงาน (แกล้งเปิด Bank)
            BankInjectSimulator.waitAndTrigger(this, mockSession)
            
        }, 5000)

        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null
    
    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, CallMonitorService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            } else {
                ctx.startService(i)
            }
        }
    }
}
"""

FILE_MAIN_ACTIVITY = """package com.riskguard.frontend.ui

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
            checkPermissionAndStart()
        }
    }

    private fun checkPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // ขอสิทธิ์ Overlay
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
        } else {
            // สิทธิ์ครบ -> เริ่มระบบ
            CallMonitorService.start(this)
            finish() // ปิดหน้า Main หนีไปเลย
        }
    }
}
"""

FILE_OVERLAY_ACTIVITY = """package com.riskguard.frontend.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.riskguard.frontend.R
import com.riskguard.frontend.network.ApiClient

class OverlayChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- KEY LOGIC: ทำให้ Activity เป็น Overlay ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setShowWhenLocked(true)
            turnScreenOn(true)
        }
        // หมายเหตุ: ใน Android ใหม่ๆ การใช้ TYPE_APPLICATION_OVERLAY ต้องทำผ่าน Service+WindowManager 
        // แต่เพื่อความง่ายใน Phase Mock เราใช้ Activity ธรรมดาที่เด้งขึ้นมาแย่ง Focus ก่อน
        // ถ้าจะทำ Overlay แบบลอยจริงๆ ต้องใช้ WindowManager.addView (Code จะซับซ้อนกว่านี้มาก)
        // โค้ดนี้ใช้เทคนิค "Full Screen Alert" แทนชั่วคราว
        
        setContentView(R.layout.activity_overlay_chat)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnAction = findViewById<Button>(R.id.btnAction)

        tvStatus.text = "กำลังวิเคราะห์ความเสี่ยง..."

        // จำลองการยิง API ไปเช็ค
        ApiClient.checkRisk { result ->
            tvStatus.text = result.summary
            if (result.level == "HIGH") {
                tvStatus.setTextColor(android.graphics.Color.RED)
            }
        }

        btnAction.setOnClickListener {
            finish() // ปิด Overlay ยอมให้ใช้ Bank ต่อ
        }
    }
}
"""

# --- XML Layouts ---

LAYOUT_MAIN = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="RiskGuard Setup"
        android:textSize="24sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="กดปุ่มเพื่อเริ่มระบบ Mock Monitor\n(ต้องอนุญาต Display Over Apps)"
        android:gravity="center" />

    <Button
        android:id="@+id/btnStart"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="Start System" />

</LinearLayout>
"""

LAYOUT_OVERLAY = """<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#D9000000"> <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="24dp"
        app:cardCornerRadius="16dp"
        app:cardElevation="10dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toTopOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="⚠️ แจ้งเตือนความปลอดภัย"
                android:textColor="#D32F2F"
                android:textSize="22sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvStatus"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="Loading..."
                android:textSize="18sp" />

            <Button
                android:id="@+id/btnAction"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:backgroundTint="#2E7D32"
                android:text="รับทราบ / ดำเนินการต่อ" />

        </LinearLayout>

    </androidx.cardview.widget.CardView>

</androidx.constraintlayout.widget.ConstraintLayout>
"""

XML_RULES = """<?xml version="1.0" encoding="utf-8"?><full-backup-content />"""
XML_DATA_RULES = """<?xml version="1.0" encoding="utf-8"?><data-extraction-rules><cloud-backup><include domain="root" /></cloud-backup><device-transfer><include domain="root" /></device-transfer></data-extraction-rules>"""

# --- File Generation Logic ---

def create_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Created: {path}")

def main():
    print("🚀 Initializing RiskGuard Project Structure...")

    # 1. Gradle & Manifest
    create_file(GRADLE_PATH, BUILD_GRADLE_CONTENT)
    create_file(MANIFEST_PATH, MANIFEST_CONTENT)
    
    # 2. Kotlin Files
    create_file(f"{PACKAGE_ROOT}/App.kt", FILE_APP)
    create_file(f"{PACKAGE_ROOT}/config/AppConfig.kt", FILE_CONFIG)
    create_file(f"{PACKAGE_ROOT}/monitor/CallMonitorService.kt", FILE_MONITOR_SERVICE)
    create_file(f"{PACKAGE_ROOT}/inject/BankInjectSimulator.kt", FILE_INJECT_SIM)
    create_file(f"{PACKAGE_ROOT}/ui/MainActivity.kt", FILE_MAIN_ACTIVITY)
    create_file(f"{PACKAGE_ROOT}/ui/OverlayChatActivity.kt", FILE_OVERLAY_ACTIVITY)
    create_file(f"{PACKAGE_ROOT}/model/Models.kt", FILE_MODELS)
    create_file(f"{PACKAGE_ROOT}/network/MockApi.kt", FILE_MOCK_API)
    create_file(f"{PACKAGE_ROOT}/network/ApiClient.kt", FILE_API_CLIENT)
    create_file(f"{PACKAGE_ROOT}/util/EventBus.kt", FILE_EVENT_BUS)

    # 3. Resources (Layouts & XMLs)
    create_file(f"{RES_ROOT}/layout/activity_main.xml", LAYOUT_MAIN)
    create_file(f"{RES_ROOT}/layout/activity_overlay_chat.xml", LAYOUT_OVERLAY)
    create_file(f"{RES_ROOT}/xml/backup_rules.xml", XML_RULES)
    create_file(f"{RES_ROOT}/xml/data_extraction_rules.xml", XML_DATA_RULES)
    
    # 4. Dummy Icons (Prevent build fail)
    # Note: In a real scenario, use actual icons. Here we just ensure folders exist.
    os.makedirs(f"{RES_ROOT}/mipmap-hdpi", exist_ok=True)

    print("\n✅ Project Generation Complete!")
    print("👉 Next Steps:")
    print("1. Open this folder in Android Studio (or VS Code with Android Extensions).")
    print("2. Sync Gradle.")
    print("3. Run 'MainActivity' on Emulator.")
    print("4. Click 'Start System', then wait ~5 seconds for the simulation.")

if __name__ == "__main__":
    main()