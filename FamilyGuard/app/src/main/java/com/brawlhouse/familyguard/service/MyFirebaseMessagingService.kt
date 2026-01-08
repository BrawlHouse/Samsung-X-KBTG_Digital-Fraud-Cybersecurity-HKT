package com.brawlhouse.familyguard.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.brawlhouse.familyguard.MainActivity
import com.brawlhouse.familyguard.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // ทำงานเมื่อได้รับข้อความจาก Backend
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // ดึงข้อมูล Data Payload ที่ Backend ส่งมา
        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: "Family Guard Alert"
        val body = remoteMessage.notification?.body ?: "มีการแจ้งเตือนความเสี่ยง"

        // ถ้า Backend ส่งคำสั่ง risk_alert มา
        if (data["action"] == "risk_alert") {
            showUrgentNotification(title, body, data)
        }
    }

    // สร้างการแจ้งเตือนที่เด้งขึ้นมา
    private fun showUrgentNotification(title: String, message: String, data: Map<String, String>) {
        val channelId = "fraud_alert_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // เตรียม Intent เพื่อเปิดหน้า MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // ยัดข้อมูลใส่ Intent เพื่อให้ MainActivity เปิดหน้าจอ Approve/Reject ถูกต้อง
            putExtra("action", data["action"])
            putExtra("transaction_id", data["transaction_id"])
            putExtra("risk_score", data["risk_score"])
            putExtra("reasons", data["reasons"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // สร้าง Channel สำหรับ Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fraud Alerts",
                NotificationManager.IMPORTANCE_HIGH // สำคัญมาก: ต้องใช้ HIGH เพื่อให้เด้งและมีเสียง
            ).apply {
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // เปลี่ยนเป็น icon ตกใจได้ถ้ามี
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // เด้งทันที
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    override fun onNewToken(token: String) {
        // ส่วนนี้จัดการใน MainViewModel แล้ว แต่ใส่ไว้กัน error
        super.onNewToken(token)
    }
}