package com.riskguard.frontend.inject

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
