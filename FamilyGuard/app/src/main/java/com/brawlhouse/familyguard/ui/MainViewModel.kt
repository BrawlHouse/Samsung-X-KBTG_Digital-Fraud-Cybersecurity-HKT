package com.brawlhouse.familyguard.viewmodel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.brawlhouse.familyguard.data.*
import com.brawlhouse.familyguard.ui.Screen
import com.brawlhouse.familyguard.ui.screens.RiskType // ต้องมี enum RiskType ใน Screen.kt หรือสร้างใหม่
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import java.util.UUID

class MainViewModel : ViewModel() {

    // --- State สำหรับ Login ---
    var email by mutableStateOf("")
    var password by mutableStateOf("")

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

    // --- Functions ---
    fun onEmailChange(newVal: String) { email = newVal }
    fun onPasswordChange(newVal: String) { password = newVal }

    fun onRegNicknameChange(v: String) { regNickname = v }
    fun onRegEmailChange(v: String) { regEmail = v }
    fun onRegPasswordChange(v: String) { regPassword = v }
    fun onRegBankChange(v: String) { regBankAccount = v }
    fun onRegRoleChange(v: String) { regRole = v }
    
    fun onJoinCodeChange(v: String) { joinCodeInput = v }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun showRiskDialog() { _showRiskDialog.value = true }
    fun dismissRiskDialog() { _showRiskDialog.value = false }

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
                val request = RegisterRequest(
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
                val answerList = listOf(
                    answersMap["q1"].toString(), // Who
                    answersMap["q2"].toString(), // Relationship
                    answersMap["q3"].toString(), // Profession
                    answersMap["q4"].toString(), // Action
                    answersMap["q5"].toString()  // Urgency
                )

                // สร้าง Request (Mock จำนวนเงินไปก่อน)
                val request = AnalyzeRequest(
                    answers = answerList,
                    amount = 0.0,
                    destination = "Unknown"
                )

                // ยิงไป Backend
                val response = RetrofitClient.instance.analyzeRisk(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    // อ่านค่า score ที่ Backend คำนวณมาให้
                    val riskScore = body?.aiResult?.riskScore ?: 0

                    // เปลี่ยนหน้าตามความเสี่ยง
                    if (riskScore >= 80) {
                        // Backend จะเป็นคนส่ง FCM ให้ครอบครัวเอง เราแค่เปลี่ยนหน้า
                        navigateTo(Screen.RiskResult(RiskType.HighRisk))
                    } else {
                        navigateTo(Screen.RiskResult(RiskType.LowRisk))
                    }
                } else {
                    errorMessage = "ส่งข้อมูลไม่สำเร็จ: ${response.code()}"
                }
            } catch (e: Exception) {
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
        Log.d("FCM_TEST", token)  // ← Token ยาว ๆ จะโผล่ตรงนี้

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
    fun respondToRisk(transactionId: Int, isApproved: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val action = if (isApproved) "approve" else "reject"
                val request = RespondTransactionRequest(transactionId, action)
                
                val response = RetrofitClient.instance.respondToTransaction(request)
                
                if (response.isSuccessful) {
                    // ทำรายการสำเร็จ -> กลับหน้า Dashboard
                    navigateTo(Screen.Dashboard)
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
        
        RetrofitClient.authToken = null 

        navigateTo(Screen.Login)
    }
}