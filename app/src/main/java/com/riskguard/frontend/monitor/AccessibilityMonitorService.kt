package com.riskguard.frontend.monitor

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AccessibilityMonitorService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = it.packageName?.toString()
                Log.d("AccessMonitor", "Detected App: $packageName")
                
                if (packageName == "com.kasikorn.retail.mbanking.wap") {
                    Log.d("AccessMonitor", "Target App Detected! Starting System...")
                    CallMonitorService.start(this)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Required method
    }
}
