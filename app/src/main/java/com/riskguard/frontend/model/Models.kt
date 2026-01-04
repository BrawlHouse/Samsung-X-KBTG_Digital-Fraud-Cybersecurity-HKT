package com.riskguard.frontend.model

import java.io.Serializable

data class CallSession(
    val phoneNumber: String,
    val durationSec: Long
) : Serializable

data class RiskResult(
    val score: Int,
    val level: String,
    val summary: String
)
