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

    private var callStartTime: Long? = null

    // 15 minutes in milliseconds
    private val CALL_DURATION_THRESHOLD_MS = 15 * 1000

    private lateinit var telephonyManager: android.telephony.TelephonyManager
    private lateinit var audioManager: android.media.AudioManager

    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(android.telephony.TelephonyManager::class.java)
        audioManager =
                getSystemService(android.content.Context.AUDIO_SERVICE) as
                        android.media.AudioManager
        registerPhoneStateListener()
    }

    private fun registerPhoneStateListener() {
        // Standard Phone Call Listener (SIM Card)
        try {
            telephonyManager.listen(
                    object : android.telephony.PhoneStateListener() {
                        @Deprecated("Deprecated in Java")
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            if (state == android.telephony.TelephonyManager.CALL_STATE_OFFHOOK) {
                                trySetStartTime()
                            } else if (state == android.telephony.TelephonyManager.CALL_STATE_IDLE
                            ) {
                                // Check VoIP before clearing (because user might switch from SIM to
                                // VoIP or vice versa, reasonably rare but possible to handle)
                                checkVoipState()
                            }
                        }
                    },
                    android.telephony.PhoneStateListener.LISTEN_CALL_STATE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Always update VoIP state on every event to track start time as accurately as possible
        checkVoipState()

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (isUserAtRisk && sensitivePackages.contains(packageName)) {

                // Check call logic: Must be in call AND > 15 minutes (works for both SIM & VoIP)
                if (isCallConditionMet()) {
                    // Redirect user back to our app
                    val intent =
                            Intent(this, MainActivity::class.java).apply {
                                addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                )
                                putExtra("NAVIGATE_TO", "SURVEY")
                            }
                    startActivity(intent)
                }
            }
        }
    }

    private fun isCallConditionMet(): Boolean {
        // If callStartTime is set, we are in a call (according to our listener).
        // Check if duration exceeds threshold.
        val startTime = callStartTime ?: return false
        val duration = System.currentTimeMillis() - startTime
        return duration > CALL_DURATION_THRESHOLD_MS
    }

    private fun checkVoipState() {
        try {
            // Mode 3 = MODE_IN_COMMUNICATION (VoIP like Discord, Line, Messenger)
            // Mode 2 = MODE_IN_CALL (Standard Phone)
            val mode = audioManager.mode
            if (mode == android.media.AudioManager.MODE_IN_COMMUNICATION ||
                            mode == android.media.AudioManager.MODE_IN_CALL
            ) {
                trySetStartTime()
            } else {
                // Also check Telephony directly just in case AudioManager is delayed
                // accessing telephonyManager.callState requires READ_PHONE_STATE permission
                // If not granted, this might throw SecurityException
                try {
                    val callState = telephonyManager.callState
                    if (callState == android.telephony.TelephonyManager.CALL_STATE_IDLE) {
                        // Really Idle
                        callStartTime = null
                    }
                } catch (e: SecurityException) {
                    // Permission not granted, ignore or handle gracefully
                    // If we can't check, we assume state hasn't changed from audioManager
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trySetStartTime() {
        if (callStartTime == null) {
            callStartTime = System.currentTimeMillis()
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}
