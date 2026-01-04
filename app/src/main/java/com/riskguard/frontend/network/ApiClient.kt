package com.riskguard.frontend.network

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
