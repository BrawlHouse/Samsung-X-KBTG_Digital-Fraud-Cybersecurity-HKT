package com.brawlhouse.familyguard.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.brawlhouse.familyguard.MainActivity

class RiskDetectionAccessibilityService : AccessibilityService() {

    // Apps to monitor
    private val sensitivePackages =
            listOf("com.kasikorn.retail.mbanking.wap", "com.google.android.youtube")

    // TODO: Connect this to your actual app logic/persistence to check if the specific user really
    // is high risk.
    // For now, we assume true to demonstrate the feature.
    private val isUserAtRisk: Boolean = true

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (isUserAtRisk && sensitivePackages.contains(packageName)) {
                // Redirect user back to our app
                val intent =
                        Intent(this, MainActivity::class.java).apply {
                            addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            )
                            putExtra("NAVIGATE_TO", "SURVEY")
                        }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}
