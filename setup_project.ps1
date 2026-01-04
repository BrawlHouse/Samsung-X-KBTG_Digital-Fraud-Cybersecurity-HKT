# ชื่อโปรเจกต์
$projectName = "flutter_demo_killer"

# 1. สร้างโปรเจกต์ Flutter ถ้ายังไม่มี
if (-not (Test-Path $projectName)) {
    Write-Host "Creating Flutter project: $projectName..."
    flutter create $projectName
} else {
    Write-Host "Project $projectName already exists."
}

# เข้าไปในโฟลเดอร์โปรเจกต์
Set-Location $projectName

# 2. สร้างโครงสร้างโฟลเดอร์ใน lib
$dirs = @(
    "lib/screens",
    "lib/models",
    "lib/services",
    "lib/widgets",
    "lib/utils"
)

foreach ($dir in $dirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Created directory: $dir"
    }
}

# 3. ฟังก์ชันสำหรับสร้างไฟล์พร้อม Basic Code
function Create-File ($path, $content) {
    if (-not (Test-Path $path)) {
        Set-Content -Path $path -Value $content
        Write-Host "Created file: $path"
    }
}

# --- กำหนดเนื้อหาไฟล์ ---

# MAIN
Create-File "lib/main.dart" @"
import 'package:flutter/material.dart';
import 'app.dart';

void main() {
  runApp(const MyApp());
}
"@

# APP
Create-File "lib/app.dart" @"
import 'package:flutter/material.dart';
import 'screens/incoming_call_screen.dart';

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo Killer',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const IncomingCallScreen(),
    );
  }
}
"@

# SCREENS
Create-File "lib/screens/incoming_call_screen.dart" "import 'package:flutter/material.dart';`nclass IncomingCallScreen extends StatelessWidget { const IncomingCallScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(appBar: AppBar(title: const Text('Incoming Call')), body: const Center(child: Text('Incoming Call Screen'))); } }"
Create-File "lib/screens/transfer_money_screen.dart" "import 'package:flutter/material.dart';`nclass TransferMoneyScreen extends StatelessWidget { const TransferMoneyScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(body: Center(child: Text('Transfer Money (Honeypot)'))); } }"
Create-File "lib/screens/red_alert_screen.dart" "import 'package:flutter/material.dart';`nclass RedAlertScreen extends StatelessWidget { const RedAlertScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(backgroundColor: Colors.red, body: Center(child: Text('RED ALERT!'))); } }"
Create-File "lib/screens/guardian_notify_screen.dart" "import 'package:flutter/material.dart';`nclass GuardianNotifyScreen extends StatelessWidget { const GuardianNotifyScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(body: Center(child: Text('Notifying Guardian...'))); } }"
Create-File "lib/screens/blocked_screen.dart" "import 'package:flutter/material.dart';`nclass BlockedScreen extends StatelessWidget { const BlockedScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(body: Center(child: Text('Scammer Blocked'))); } }"
Create-File "lib/screens/ai_chat_screen.dart" "import 'package:flutter/material.dart';`nclass AIChatScreen extends StatelessWidget { const AIChatScreen({super.key}); @override Widget build(BuildContext context) { return Scaffold(body: Center(child: Text('AI Chat Analysis'))); } }"

# MODELS
Create-File "lib/models/user.dart" "class User { final String id; final String name; User({required this.id, required this.name}); }"

# SERVICES
Create-File "lib/services/call_service.dart" "class CallService { void listenToCall() {} }"
Create-File "lib/services/money_service.dart" "class MoneyService { void fakeTransfer() {} }"
Create-File "lib/services/notification_service.dart" "class NotificationService { void alertGuardian() {} }"
Create-File "lib/services/ai_service.dart" "class AIService { void analyzeContext() {} }"

# WIDGETS
Create-File "lib/widgets/common_widgets.dart" "import 'package:flutter/material.dart';`nclass CustomButton extends StatelessWidget { @override Widget build(BuildContext context) { return Container(); } }"

# UTILS
Create-File "lib/utils/constants.dart" "class Constants { static const String appName = 'Demo Killer'; }"

Write-Host "`n✅ Setup Complete! You can now open '$projectName' in VS Code."