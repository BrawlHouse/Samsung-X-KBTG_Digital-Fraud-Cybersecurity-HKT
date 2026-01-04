package com.riskguard.frontend.util

import com.riskguard.frontend.model.CallSession

// EventBus แบบบ้านๆ สำหรับสื่อสารระหว่าง Service กับ Simulator
object EventBus {
    private var listener: ((CallSession) -> Unit)? = null

    fun register(l: (CallSession) -> Unit) {
        listener = l
    }

    fun emit(session: CallSession) {
        listener?.invoke(session)
    }
}
