package com.riskguard.frontend.network

import android.os.Handler
import android.os.Looper
import com.riskguard.frontend.model.RiskResult

object MockApi {
    fun evaluateRisk(answers: Map<String,String>, callback: (RiskResult) -> Unit) {
        // คำนวณคะแนนง่าย ๆ
        var score = 0
        if(answers["รู้จักเป็นการส่วนตัวไหม"] != "รู้จัก") score += 30
        if(answers["เขาขอให้คุณทำอะไร"] in listOf("โอนเงิน","ให้รหัส","ติดตั้งแอป")) score += 40
        if(answers["เขากดดันเรื่องความเร่งด่วน"] == "มี") score += 20
        val level = when {
            score >= 70 -> "HIGH"
            score >= 40 -> "MEDIUM"
            else -> "LOW"
        }
        val summary = when(level){
            "HIGH" -> "⚠️ เสี่ยงสูง! โปรดตรวจสอบกับครอบครัว"
            "MEDIUM" -> "เสี่ยงปานกลาง"
            else -> "ความเสี่ยงต่ำ"
        }
        Handler(Looper.getMainLooper()).postDelayed({
            callback(RiskResult(score, level, summary))
        }, 500)
    }
}
