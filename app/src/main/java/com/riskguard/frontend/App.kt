package com.riskguard.frontend

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
