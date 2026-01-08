    const admin = require('../config/firebase');

    const sendPushNotification = async (fcmToken, title, body, dataPayload = {}) => {
        if (!fcmToken) {
            console.warn("⚠️ No FCM Token provided. Skipping notification.");
            return;
        }
        
        // แปลง value ใน dataPayload ให้เป็น String ทั้งหมด (Firebase บังคับ)
        const stringifiedData = {};
        for (const key in dataPayload) {
            stringifiedData[key] = String(dataPayload[key]);
        }

        const message = {
            token: fcmToken,
            notification: {
                title: title,
                body: body
            },
            // Data Payload: Android จะได้รับสิ่งนี้ใน onMessageReceived
            data: stringifiedData, 
            android: {
                priority: 'high',
                notification: {
                    channelId: 'fraud_alert_channel',
                    sound: 'default',
                    priority: 'high', // Heads-up notification
                    visibility: 'public'
                }
            }
        };
        
        try {
            const response = await admin.messaging().send(message);
            console.log('✅ Notification sent successfully:', response);
            return response;
        } catch (error) {
            console.error('❌ Error sending notification:', error);
            // ไม่ throw error เพื่อให้ process หลักทำงานต่อได้
        }
    };

    module.exports = { sendPushNotification };