package com.brawlhouse.familyguard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brawlhouse.familyguard.data.*  // เพิ่มตามที่ขอ (รวม FamilyMember, JoinFamilyRequest ฯลฯ)
import com.brawlhouse.familyguard.data.LoginRequest
import com.brawlhouse.familyguard.data.RegisterRequest
import com.brawlhouse.familyguard.data.RetrofitClient
import com.brawlhouse.familyguard.ui.Screen
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
    // เก็บรายชื่อสมาชิกที่ดึงมาจาก API
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers = _familyMembers.asStateFlow()

    // เก็บ Invite Code ของครอบครัวเรา
    var myInviteCode by mutableStateOf("")

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

    // Register Field Update Functions
    fun onRegNicknameChange(v: String) { regNickname = v }
    fun onRegEmailChange(v: String) { regEmail = v }
    fun onRegPasswordChange(v: String) { regPassword = v }
    fun onRegBankChange(v: String) { regBankAccount = v }
    fun onRegRoleChange(v: String) { regRole = v }

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
                    RetrofitClient.authToken = body?.token  // <--- เก็บ token ตามที่ขอ

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
                // สร้าง Device ID แบบสุ่ม (สำหรับการทดสอบ)
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

    // 1. สร้างครอบครัว
    fun createFamily() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.createFamily()
                if (response.isSuccessful) {
                    val body = response.body()
                    myInviteCode = body?.inviteCode ?: ""
                    navigateTo(Screen.Dashboard)  // สร้างเสร็จไปหน้า Dashboard
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

    // 2. เข้าร่วมครอบครัว
    fun joinFamily(code: String) {
        if (code.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.joinFamily(JoinFamilyRequest(code))
                if (response.isSuccessful) {
                    navigateTo(Screen.Dashboard)  // เข้าเสร็จไปหน้า Dashboard
                } else {
                    errorMessage = "Invalid Invite Code"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // 3. ดึงข้อมูลสมาชิก (ใช้ใน Dashboard)
    fun loadFamilyData() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getFamilyMembers()
                if (response.isSuccessful) {
                    val body = response.body()
                    _familyMembers.value = body?.members ?: emptyList()
                    myInviteCode = body?.inviteCode ?: ""
                }
            } catch (e: Exception) {
                // Handle error (อาจตั้ง errorMessage ถ้าต้องการแจ้งผู้ใช้)
                errorMessage = e.message
            }
        }
    }

    // 4. ลบสมาชิก
    fun removeMember(userId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.removeMember(userId)
                loadFamilyData()  // โหลดข้อมูลใหม่หลังลบ
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }
}