package com.brawlhouse.familyguard.viewmodel

// หรือสร้างใหม่
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brawlhouse.familyguard.data.*
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.RiskType
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("family_guard_prefs", Context.MODE_PRIVATE)

    private fun checkSession() {
        try {
            val token = prefs.getString("auth_token", null)
            val userId = prefs.getInt("user_id", 0)

            if (!token.isNullOrEmpty() && userId != 0) {
                // Restore session
                RetrofitClient.authToken = token
                currentUserId = userId
                _currentScreen.value = Screen.Dashboard

                // Sync/Refresh data safely
                loadFamilyData()
                try {
                    syncFcmToken()
                } catch (e: Exception) {
                    Log.e("MainViewModel", "FCM Sync failed during session restore", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error restoring session", e)
            // Do not clear prefs automatically to avoid loop if transient error
        }
    }

    // --- State สำหรับ Login ---
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // ID สำหรับ Polling
    var currentUserId by mutableStateOf(0)

    // --- State สำหรับ Register ---
    var regNickname by mutableStateOf("")
    var regEmail by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regBankAccount by mutableStateOf("")
    var regRole by mutableStateOf("parent")

    // --- Family State ---
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()

    var myInviteCode by mutableStateOf("")
    var joinCodeInput by mutableStateOf("")

    // UI Control
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Navigation Logic
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen = _currentScreen.asStateFlow()

    private val _showRiskDialog = MutableStateFlow(false)
    val showRiskDialog = _showRiskDialog.asStateFlow()

    init {
        checkSession()
    }

    // --- Functions ---
    fun onEmailChange(newVal: String) {
        email = newVal
    }
    fun onPasswordChange(newVal: String) {
        password = newVal
    }

    fun onRegNicknameChange(v: String) {
        regNickname = v
    }
    fun onRegEmailChange(v: String) {
        regEmail = v
    }
    fun onRegPasswordChange(v: String) {
        regPassword = v
    }
    fun onRegBankChange(v: String) {
        regBankAccount = v
    }
    fun onRegRoleChange(v: String) {
        regRole = v
    }

    fun onJoinCodeChange(v: String) {
        joinCodeInput = v
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showRiskDialog() {
        _showRiskDialog.value = true
    }
    fun dismissRiskDialog() {
        _showRiskDialog.value = false
    }

    // --- API: Login ---
    fun login() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "กรุณากรอกข้อมูลให้ครบถ้วน"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    RetrofitClient.authToken = body?.token
                    currentUserId = body?.user?.userId ?: 0

                    Log.d(
                            "SESSION_DEBUG",
                            "Login Success. Saving session -> UserID: $currentUserId"
                    )
                    // Save session
                    prefs.edit()
                            .putString("auth_token", body?.token)
                            .putInt("user_id", currentUserId)
                            .apply()

                    // [สำคัญ] ล็อกอินเสร็จ ต้องส่ง FCM Token ไปให้ Backend รู้จักเครื่องนี้
                    syncFcmToken()

                    navigateTo(Screen.Dashboard)
                } else {
                    errorMessage = "Login Failed: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- API: Register ---
    fun register() {
        if (regEmail.isBlank() || regPassword.isBlank() || regNickname.isBlank()) {
            errorMessage = "กรุณากรอกข้อมูลสำคัญให้ครบ (ชื่อ, อีเมล, รหัสผ่าน)"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val dummyDeviceId = "android_" + UUID.randomUUID().toString().substring(0, 8)
                val request =
                        RegisterRequest(
                                email = regEmail,
                                password = regPassword,
                                nickname = regNickname,
                                role = regRole,
                                deviceId = dummyDeviceId,
                                bankAccountNumber = regBankAccount.ifBlank { "000-0-00000-0" }
                        )
                val response = RetrofitClient.instance.register(request)
                if (response.isSuccessful) {
                    errorMessage = null
                    navigateTo(Screen.Login)
                } else {
                    errorMessage = "สมัครสมาชิกไม่สำเร็จ (อาจมีอีเมลนี้แล้ว)"
                }
            } catch (e: Exception) {
                errorMessage = "เชื่อมต่อ Server ไม่ได้: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // --- API: Create Family ---
    fun createFamily() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.createFamily()
                if (response.isSuccessful) {
                    val body = response.body()
                    myInviteCode = body?.inviteCode ?: ""
                    navigateTo(Screen.Dashboard)
                } else {
                    errorMessage = "Create failed: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // --- API: Join Family ---
    fun joinFamily() {
        if (joinCodeInput.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.joinFamily(JoinFamilyRequest(joinCodeInput))
                if (response.isSuccessful) {
                    loadFamilyData()
                    navigateTo(Screen.Dashboard)
                } else {
                    errorMessage = "รหัสไม่ถูกต้อง หรือเข้าร่วมไม่ได้"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // --- API: Get Family Data ---
    fun loadFamilyData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = RetrofitClient.instance.getFamilyMembers()
                if (response.isSuccessful) {
                    val body = response.body()
                    _familyMembers.value = body?.members ?: emptyList()
                    myInviteCode = body?.inviteCode ?: ""
                } else {
                    myInviteCode = ""
                    _familyMembers.value = emptyList()
                }
            } catch (e: Exception) {
                errorMessage = e.message
                myInviteCode = ""
            } finally {
                isLoading = false
            }
        }
    }

    // --- API: Remove Member ---
    fun removeMember(userId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.removeMember(userId)
                loadFamilyData()
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    // ==========================================
    // ส่วนที่เพิ่มใหม่ (New Logic for Survey & FCM)
    // ==========================================

    // 1. ส่งคำตอบ Survey ไป Backend (Logic อยู่ที่ Node.js)
    fun submitSurvey(answersMap: Map<String, Any>) {
        viewModelScope.launch {
            isLoading = true
            try {
                // แปลง Map จาก UI เป็น List ตามลำดับ
                val answerList =
                        listOf(
                                answersMap["q1"].toString(), // Who
                                answersMap["q2"].toString(), // Relationship
                                answersMap["q3"].toString(), // Profession
                                answersMap["q4"].toString(), // Action
                                answersMap["q5"].toString() // Urgency
                        )

                // สร้าง Request
                val request = AnalyzeRequest(answers = answerList)

                // ยิงไป Backend
                val response = RetrofitClient.instance.analyzeRisk(request)

                Log.d("SURVEY_RES", "Status Code: ${response.code()}")
                Log.d("SURVEY_RES", "Response Body: ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    // อ่านค่า score ที่ Backend คำนวณมาให้
                    val riskScore = body?.aiResult?.riskScore ?: 0

                    // เปลี่ยนหน้าตามความเสี่ยง
                    if (riskScore >= 80) {
                        // Backend จะเป็นคนส่ง FCM ให้ครอบครัวเอง เราแค่เปลี่ยนหน้า
                        navigateTo(Screen.RiskResult(RiskType.ApprovalRequired))
                    } else {
                        navigateTo(Screen.RiskResult(RiskType.LowRisk))
                    }
                } else {
                    Log.e("SURVEY_RES", "Error Code: ${response.code()}")
                    Log.e("SURVEY_RES", "Error Body: ${response.errorBody()?.string()}")
                    errorMessage = "ส่งข้อมูลไม่สำเร็จ: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("SURVEY_RES", "Exception: ${e.message}", e)
                errorMessage = "Network Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // 2. ดึง FCM Token แล้วส่งไปอัปเดตที่ Backend
    fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_TEST", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TEST", "=== ได้ FCM Token แล้ว ===")
            Log.d("FCM_TEST", token) // ← Token ยาว ๆ จะโผล่ตรงนี้

            viewModelScope.launch {
                try {
                    RetrofitClient.instance.updateFcmToken(UpdateTokenRequest(token))
                    Log.d("FCM_TEST", "ส่ง FCM Token ไป Backend สำเร็จ")
                } catch (e: Exception) {
                    Log.e("FCM_TEST", "ส่ง Token ไป Backend ล้มเหลว", e)
                }
            }
        }
    }
    // --- Active Risk Alert Data ---
    var activeRiskTransactionId by mutableStateOf(0)
    var activeRiskNickname by mutableStateOf("")
    var activeRiskMessages by mutableStateOf<List<String>>(emptyList())

    fun setRiskData(txId: Int, nickname: String, rawMessage: String) {
        activeRiskTransactionId = txId
        activeRiskNickname = nickname
        try {
            // Simple manual parsing [ "a", "b" ] or use Gson if available
            // สมมติว่าเป็น JSON Array String
            val cleaned = rawMessage.trim().removePrefix("[").removeSuffix("]")
            if (cleaned.isBlank()) {
                activeRiskMessages = emptyList()
            } else {
                // Split by comma, then remove quotes
                activeRiskMessages =
                        cleaned.split(",").map {
                            it.trim().removeSurrounding("\"").removeSurrounding("'")
                        }
            }
        } catch (e: Exception) {
            activeRiskMessages = listOf(rawMessage)
        }
        _showRiskDialog.value = true
    }

    fun respondToRisk(transactionId: Int, isAllowed: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val action = if (isAllowed) "allow" else "reject"
                val request = RespondTransactionRequest(transactionId, action)

                val response = RetrofitClient.instance.respondToTransaction(request)

                if (response.isSuccessful) {
                    // ทำรายการสำเร็จ -> ปิด Dialog / กลับหน้า Dashboard
                    _showRiskDialog.value = false
                    // navigateTo(Screen.Dashboard) // Already likely on dashboard if using dialog
                } else {
                    errorMessage = "Failed: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // ==========================================

    fun logout() {
        email = ""
        password = ""
        regNickname = ""
        regEmail = ""
        regPassword = ""
        regBankAccount = ""
        myInviteCode = ""
        joinCodeInput = ""
        _familyMembers.value = emptyList()
        errorMessage = null
        isLoading = false

        // Clear session
        prefs.edit().clear().apply()
        RetrofitClient.authToken = null

        navigateTo(Screen.Login)
    }

    // --- API: Get User Status for Polling ---
    fun checkUserStatus() {
        viewModelScope.launch {
            try {
                // สมมติว่า userId ถูกเก็บไว้ใน SharedPreferences หรือตัวแปร global
                // แต่ในโค้ดปัจจุบันเรายังไม่มีที่เก็บ userId ที่ชัดเจนหลัง login
                // ดังนั้นเราจะใช้ UserData จาก Token หรือ Response ตอน Login ถ้ามีการเก็บไว้
                // เพื่อความง่าย ผมจะ hardcode หรือดึงจาก familyMembers ถ้ามี
                // หรือวิธีที่ดีคือ เก็บ userId ไว้ใน ViewModel ตอน Login สำเร็จ
                // แต่ตอนนี้ขอสมมติว่าเรามี userId เก็บไว้แล้ว หรือส่งมา

                // เพื่อให้โค้ดทำงานได้จริง เราต้องเก็บ userId ตอน login
                // ผมจะเพิ่ม userId var ใน viewModel นี้แหละครับ
                if (currentUserId == 0) return@launch

                val response = RetrofitClient.instance.getUserStatus(currentUserId)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    if (status == "allow") {
                        navigateTo(Screen.RiskResult(RiskType.LowRisk))
                    } else if (status == "reject") {
                        navigateTo(Screen.RiskResult(RiskType.HighRisk))
                    }
                }
            } catch (e: Exception) {
                Log.e("POLLING", "Error: ${e.message}")
            }
        }
    }
}
