package com.riskguard.frontend.inject

import android.content.Context
import android.content.Intent
import com.riskguard.frontend.model.CallSession
import com.riskguard.frontend.ui.OverlayChatActivity

object BankInjectSimulator {

    fun trigger(ctx: Context, session: CallSession) {
        val i = Intent(ctx, OverlayChatActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("session", session)
        }
        ctx.startActivity(i)
    }
}
