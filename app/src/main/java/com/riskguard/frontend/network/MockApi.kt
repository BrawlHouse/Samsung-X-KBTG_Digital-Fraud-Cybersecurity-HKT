package com.riskguard.frontend.network

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
                    summary = "!! เตือนภัย !!\nปลายสายมีการเร่งรัดให้โอนเงิน\nและคุยนานผิดปกติ"
                )
            )
        }, 1500)
    }
}
