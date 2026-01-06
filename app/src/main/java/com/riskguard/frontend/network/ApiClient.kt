package com.riskguard.frontend.network

import com.riskguard.frontend.config.AppConfig
import com.riskguard.frontend.model.RiskResult

object ApiClient {

    // ส่ง answers จาก form (Map<String,String>) เข้า evaluateRisk
    fun checkRisk(
        answers: Map<String,String>,
        callback: (RiskResult) -> Unit
    ) {
        if (AppConfig.IS_MOCK_MODE) {
            MockApi.evaluateRisk(answers, callback)
        } else {
            // TODO: ต่อ Node.js จริง
        }
    }
}
