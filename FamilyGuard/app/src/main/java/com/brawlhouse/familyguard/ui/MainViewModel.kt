package com.brawlhouse.familyguard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brawlhouse.familyguard.data.LoginRequest
import com.brawlhouse.familyguard.data.RegisterRequest
import com.brawlhouse.familyguard.data.RetrofitClient
import com.brawlhouse.familyguard.ui.Screen // แก้ไขการ Import: เอา .kt ออก
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

    // UI Control
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Navigation Logic
    // แก้ไขชื่อตัวแปร: ลบ .kt และ backticks ออก
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

    // แก้ไข Parameter: รับค่าเป็น Screen ธรรมดา
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
                    navigateTo(Screen.Dashboard) // เรียกใช้ Screen.Dashboard ปกติ
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
                    // สมัครเสร็จแล้ว กลับไปหน้า Login
                    errorMessage = null
                    navigateTo(Screen.Login) // เรียกใช้ Screen.Login ปกติ
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
}