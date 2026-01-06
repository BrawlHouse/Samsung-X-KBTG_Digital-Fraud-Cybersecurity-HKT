package com.brawlhouse.familyguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brawlhouse.familyguard.viewmodel.MainViewModel

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // ScrollState ไว้เลื่อนหน้าจอเวลามี TextField เยอะๆ
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState), // ทำให้เลื่อนได้
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Nickname
        OutlinedTextField(
            value = viewModel.regNickname,
            onValueChange = { viewModel.onRegNicknameChange(it) },
            label = { Text("Nickname (ชื่อเล่น)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Email
        OutlinedTextField(
            value = viewModel.regEmail,
            onValueChange = { viewModel.onRegEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Password
        OutlinedTextField(
            value = viewModel.regPassword,
            onValueChange = { viewModel.onRegPasswordChange(it) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 4. Role Selection (Parent / Child)
        Text("Select Role", fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = viewModel.regRole == "parent",
                    onClick = { viewModel.onRegRoleChange("parent") }
                )
                Text("Parent (ผู้ปกครอง)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = viewModel.regRole == "child",
                    onClick = { viewModel.onRegRoleChange("child") }
                )
                Text("Child (ลูกหลาน)")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 5. Bank Account Number
        OutlinedTextField(
            value = viewModel.regBankAccount,
            onValueChange = { viewModel.onRegBankChange(it) },
            label = { Text("Bank Account (เลขบัญชี)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Outlined.AccountBalance, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit Button
        Button(
            onClick = { viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login
        TextButton(onClick = onBackClick) {
            Text("Already have an account? Log In")
        }
    }
}