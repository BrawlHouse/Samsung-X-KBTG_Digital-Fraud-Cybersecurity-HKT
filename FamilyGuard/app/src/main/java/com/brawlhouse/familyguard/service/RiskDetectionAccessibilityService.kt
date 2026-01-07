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

    // Separate tracking for SIM and VoIP/Audio Mode to ensure robustness
    private var simCallStartTime: Long? = null
    private var voipCallStartTime: Long? = null

    // 15 minutes in milliseconds (Testing: 15 seconds)
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
        try {
            telephonyManager.listen(
                    object : android.telephony.PhoneStateListener() {
                        @Deprecated("Deprecated in Java")
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            when (state) {
                                android.telephony.TelephonyManager.CALL_STATE_OFFHOOK -> {
                                    // Call started (or active)
                                    if (simCallStartTime == null) {
                                        simCallStartTime = System.currentTimeMillis()
                                    }
                                }
                                android.telephony.TelephonyManager.CALL_STATE_IDLE -> {
                                    // Call ended
                                    simCallStartTime = null
                                }
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

        // Check Audio Mode (VoIP/Call)
        checkAudioModeState()

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (isUserAtRisk && sensitivePackages.contains(packageName)) {

                // Check call logic
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

    private fun checkAudioModeState() {
        // Check AudioManager Mode for VoIP (Discord, Line, etc) or standard calls
        // This acts as a fallback or primary check for VoIP
        val mode = audioManager.mode

        if (mode == android.media.AudioManager.MODE_IN_COMMUNICATION ||
                        mode == android.media.AudioManager.MODE_IN_CALL
        ) {
            if (voipCallStartTime == null) {
                voipCallStartTime = System.currentTimeMillis()
            }
        } else {
            // Mode is NORMAL or RINGTONE -> No active conversation
            voipCallStartTime = null
        }
    }

    private fun isCallConditionMet(): Boolean {
        val now = System.currentTimeMillis()

        // Check SIM Call
        if (simCallStartTime != null) {
            val duration = now - simCallStartTime!!
            if (duration > CALL_DURATION_THRESHOLD_MS) return true
        }

        // Check VoIP/Audio Mode Call
        if (voipCallStartTime != null) {
            val duration = now - voipCallStartTime!!
            if (duration > CALL_DURATION_THRESHOLD_MS) return true
        }

        return false
    }

    override fun onInterrupt() {
        // Handle interruption
    }
}
