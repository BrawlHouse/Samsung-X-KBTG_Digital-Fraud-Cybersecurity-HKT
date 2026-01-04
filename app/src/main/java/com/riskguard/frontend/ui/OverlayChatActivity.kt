package com.riskguard.frontend.ui

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
        
        // --- แก้ไขปัญหา Unresolved reference และทำให้รองรับหลายเวอร์ชัน ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // สำหรับ Android 8.1 (API 27) ขึ้นไป
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // สำหรับ Android เวอร์ชั่นเก่ากว่า
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // ตั้งค่าให้ Activity แสดงผลทับหน้าจออื่น (เท่าที่ Activity จะทำได้)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        
        setContentView(R.layout.activity_overlay_chat)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnAction = findViewById<Button>(R.id.btnAction)

        tvStatus.text = "กำลังวิเคราะห์ความเสี่ยง..."

        // จำลองการยิง API ไปเช็ค
        ApiClient.checkRisk { result ->
            // ใช้ runOnUiThread เพื่อความปลอดภัยในการอัปเดต UI จาก Callback
            runOnUiThread {
                tvStatus.text = result.summary
                if (result.level == "HIGH") {
                    tvStatus.setTextColor(android.graphics.Color.RED)
                }
            }
        }

        btnAction.setOnClickListener {
            finish() // ปิดหน้าจอเตือนภัย
        }
    }
}