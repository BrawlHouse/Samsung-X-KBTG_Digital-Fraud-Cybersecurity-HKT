package com.brawlhouse.familyguard.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView
import com.brawlhouse.familyguard.MainActivity
import com.brawlhouse.familyguard.R
import com.brawlhouse.familyguard.data.AnalyzeTextRequest
import com.brawlhouse.familyguard.data.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RiskDetectionAccessibilityService : AccessibilityService() {

    // --- Call Detection ---
    private val sensitivePackages =
            listOf("com.kasikorn.retail.mbanking.wap", "com.google.android.youtube")
    private val isUserAtRisk = true
    private var simCallStartTime: Long? = null
    private var voipCallStartTime: Long? = null
    private val CALL_DURATION_THRESHOLD_MS = 15 * 1000L

    private lateinit var telephonyManager: android.telephony.TelephonyManager
    private lateinit var audioManager: android.media.AudioManager

    // --- AI Text Analysis ---
    private val messagingPackages =
            listOf(
                    "com.google.android.apps.messaging",
                    "com.facebook.orca",
                    "jp.naver.line.android",
                    "com.samsung.android.messaging"
            )

    private val riskKeywords =
            listOf(
                    "โอน",
                    "บัญชี",
                    "ธนาคาร",
                    "รางวัล",
                    "กู้",
                    "ดอกเบี้ย",
                    "งานพิเศษ",
                    "ลงทุน",
                    "ผู้โชคดี",
                    "รับสิทธิ์",
                    "ระงับ",
                    "http",
                    "www",
                    ".com",
                    ".net",
                    "bit.ly",
                    "link",
                    "cc"
            )

    private var lastAnalyzedText = ""
    private var lastAlertTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d("RiskDebug", "=== RiskDetectionAccessibilityService onCreate เริ่มทำงาน ===")
        telephonyManager = getSystemService(android.telephony.TelephonyManager::class.java)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        registerPhoneStateListener()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("RiskDebug", "=== onServiceConnected: Service เชื่อมต่อสำเร็จ! ===")
        // ไม่ต้อง log event types เพราะเรา config ใน xml แล้ว
    }

    private fun registerPhoneStateListener() {
        try {
            telephonyManager.listen(
                    object : android.telephony.PhoneStateListener() {
                        @Deprecated("Deprecated in Java")
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            when (state) {
                                android.telephony.TelephonyManager.CALL_STATE_OFFHOOK -> {
                                    if (simCallStartTime == null) {
                                        simCallStartTime = System.currentTimeMillis()
                                    }
                                }
                                android.telephony.TelephonyManager.CALL_STATE_IDLE -> {
                                    simCallStartTime = null
                                }
                            }
                        }
                    },
                    android.telephony.PhoneStateListener.LISTEN_CALL_STATE
            )
        } catch (e: Exception) {
            Log.e("RiskDebug", "PhoneStateListener error", e)
        }
    }

    private fun extractAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val sb = StringBuilder()
        if (!node.text.isNullOrBlank()) {
            sb.append(node.text).append(" ")
        }
        for (i in 0 until node.childCount) {
            sb.append(extractAllText(node.getChild(i)))
        }
        return sb.toString()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: "unknown"
        // Log.d("RiskDebug", "ได้รับ Event: type=${event.eventType} Package=$packageName") // ลด
        // log เพื่อความสะอาด

        // ดึงข้อความทั้งหน้าจอ
        val rootNode = rootInActiveWindow
        val isMessagingApp =
                messagingPackages.any { packageName.contains(it) } ||
                        packageName.contains("line") ||
                        packageName.contains("messenger") ||
                        packageName.contains("whatsapp") ||
                        packageName.contains("telegram")

        if (isMessagingApp && rootNode != null) {
            val fullText = extractAllText(rootNode)
            // Log.d("RiskDebug", "ดึงข้อความ: length=${fullText.length}") // เช็ค length พอ

            if (fullText.isNotBlank() && fullText.length > 10 && fullText != lastAnalyzedText) {

                if (isPotentiallyRiskyLocal(fullText)) {
                    Log.d("RiskDebug", "พบข้อความเสี่ยง (Local Check Passed) → ส่งไปวิเคราะห์ AI")
                    lastAnalyzedText = fullText // update state เพื่อไม่ให้ส่งซ้ำ
                    analyzeContent(fullText)
                }
            }
        }

        // Call detection logic (คงเดิม)
        checkAudioModeState()
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isUserAtRisk && sensitivePackages.contains(packageName)) {
                if (isCallConditionMet()) {
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

    private fun isPotentiallyRiskyLocal(text: String): Boolean {
        val hasLongNumbers = text.contains(Regex("\\d{10,}"))
        val hasRiskKeyword = riskKeywords.any { text.contains(it, ignoreCase = true) }
        return hasLongNumbers || hasRiskKeyword
    }

    private fun analyzeContent(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // [TWEAK] ข้ามการเช็ค Token ชั่วคราวเพื่อให้ Test Popup ได้ง่ายขึ้น
                // ถ้า Production จริง ควรเปิดกลับมา
                /*
                if (RetrofitClient.authToken == null) {
                    Log.d("RiskDebug", "ไม่มี authToken → ข้ามการวิเคราะห์")
                    return@launch
                }
                */

                Log.d("RiskDebug", "กำลังส่งข้อความไปหา AI...")
                val response = RetrofitClient.instance.analyzeText(AnalyzeTextRequest(text = text))

                if (response.isSuccessful) {
                    val result = response.body()
                    // Log.d("RiskDebug", "AI Response: $result")

                    if (result != null && result.isRisk) {
                        withContext(Dispatchers.Main) {
                            if (System.currentTimeMillis() - lastAlertTime > 5000) {
                                // [แก้ไขตรงนี้] เปลี่ยน riskScore -> percentage และ reasons ->
                                // reason
                                showRiskPopup(result.percentage, result.level, result.reason)

                                lastAlertTime = System.currentTimeMillis()
                            }
                        }
                    }
                } else {
                    Log.e(
                            "RiskDebug",
                            "API Error: ${response.code()} ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("RiskDebug", "analyzeContent error", e)
            }
        }
    }

    private fun showRiskPopup(score: Int, level: String, reason: String) {
        try {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            // [CRITICAL FIX] ใช้ TYPE_APPLICATION_OVERLAY และ FLAG_LAYOUT_IN_SCREEN
            val layoutFlag =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        WindowManager.LayoutParams.TYPE_PHONE
                    }

            val params =
                    WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT,
                                    layoutFlag,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                                    PixelFormat.TRANSLUCENT
                            )
                            .apply {
                                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                                y = 100
                            }

            // ใช้ Context ของ Service ในการ Inflate (สำคัญมาก)
            val inflater = LayoutInflater.from(this)
            val view = inflater.inflate(R.layout.layout_risk_warning, null)

            view.findViewById<TextView>(R.id.tvWarningTitle).text =
                    "⚠️ ความเสี่ยง: $score% ($level)"
            view.findViewById<TextView>(R.id.tvWarningDetails).text = "AI เตือนภัย: $reason"

            view.findViewById<Button>(R.id.btnCloseWarning).setOnClickListener {
                try {
                    windowManager.removeView(view)
                    Log.d("RiskDebug", "Popup closed by user")
                } catch (_: Exception) {}
            }

            windowManager.addView(view, params)
            Log.d("RiskDebug", "Popup added successfully (addView called)")
        } catch (e: Exception) {
            Log.e("RiskDebug", "Error showing popup: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkAudioModeState() {
        val mode = audioManager.mode
        if (mode == android.media.AudioManager.MODE_IN_COMMUNICATION ||
                        mode == android.media.AudioManager.MODE_IN_CALL
        ) {
            if (voipCallStartTime == null) voipCallStartTime = System.currentTimeMillis()
        } else {
            voipCallStartTime = null
        }
    }

    private fun isCallConditionMet(): Boolean {
        val now = System.currentTimeMillis()
        simCallStartTime?.let { if (now - it > CALL_DURATION_THRESHOLD_MS) return true }
        voipCallStartTime?.let { if (now - it > CALL_DURATION_THRESHOLD_MS) return true }
        return false
    }

    override fun onInterrupt() {}
}
