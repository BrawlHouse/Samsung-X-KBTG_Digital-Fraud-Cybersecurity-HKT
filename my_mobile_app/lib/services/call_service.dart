import 'dart:io';
import 'package:phone_state/phone_state.dart'; // ✅ ใช้ตัวนี้แทน telephony
import 'package:permission_handler/permission_handler.dart';
import 'notification_service.dart';

class CallService {
  
  // ฟังก์ชันเริ่มต้น (เรียกใช้ตอนเปิดแอป)
  static Future<void> initCallListener() async {
    // 1. ขอ Permission ก่อน (จำเป็นสำหรับ Android)
    if (Platform.isAndroid) {
      await _requestPermissions();
    }

    // 2. เริ่มดักฟังสถานะโทรศัพท์ (Stream)
    PhoneState.stream.listen((event) async {
      print("📞 Phone Status: ${event.status}");

      // เมื่อมีสายเรียกเข้า (Incoming Call)
      if (event.status == PhoneStateStatus.CALL_INCOMING) {
        String? incomingNumber = event.number; // ดึงเบอร์โทร
        print("ScamGuard Checking: $incomingNumber");

        if (incomingNumber != null && isUnknownNumber(incomingNumber)) {
          // แจ้งเตือนทันที!
          await NotificationService.showNotification(
            title: "⚠️ ระวัง! สายเข้าเบอร์แปลก",
            body: "เบอร์ $incomingNumber อาจเป็นมิจฉาชีพ (AI กำลังจับตาดู)",
          );
          
          // TODO: ตรงนี้สามารถสั่งให้เด้งหน้า Red Alert Screen ได้
        }
      }
    });
  }

  // ฟังก์ชันขออนุญาตเข้าถึงโทรศัพท์
  static Future<void> _requestPermissions() async {
    await [
      Permission.phone,
      Permission.microphone, // อาจต้องใช้ถ้าจะดักเสียง (ในอนาคต)
    ].request();
  }

  // Logic ตรวจสอบเบอร์ (ปรับแต่งได้)
  static bool isUnknownNumber(String number) {
    // ตัวอย่าง: ถ้าเบอร์ยาวกว่า 8 หลัก ให้ถือว่าต้องสงสัยไว้ก่อน
    return number.length > 8; 
  }
}