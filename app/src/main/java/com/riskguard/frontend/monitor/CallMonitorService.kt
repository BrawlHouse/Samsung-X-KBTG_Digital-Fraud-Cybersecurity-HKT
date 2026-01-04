package com.riskguard.frontend.monitor

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
