package com.riskguard.frontend.monitor

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.riskguard.frontend.inject.BankInjectSimulator
import com.riskguard.frontend.model.CallSession

class CallMonitorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Mock flow: simulate call session
        Handler(Looper.getMainLooper()).postDelayed({
            val session = CallSession("089-999-9999", 600)
            BankInjectSimulator.trigger(this, session)
        }, 2000)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(ctx: android.content.Context) {
            val i = Intent(ctx, CallMonitorService::class.java)
            ctx.startService(i)
        }
    }
}
