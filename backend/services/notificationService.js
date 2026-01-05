const admin = require('../config/firebase'); // เรียกใช้ตัวที่เรา config ไว้ตอนแรก

const sendPushNotification = async (fcmToken, title, body, dataPayload = {}) => {
    if (!fcmToken) {
        console.log("No FCM Token provided, skipping notification.");
        return;
    }
    
    // สร้าง Payload ข้อความตามมาตรฐาน FCM
    const message = {
        notification: {
            title: title,
            body: body
        },
        // data เอาไว้ส่งค่า transaction_id แฝงไปกับแจ้งเตือน (user ไม่เห็น แต่แอปเอาไปใช้ต่อได้)
        data: dataPayload, 
        token: fcmToken,
        
        // ตั้งค่า Priority สูงสุด เพื่อให้แจ้งเตือนเด้งทันที
        android: {
            priority: 'high',
            notification: {
                channelId: 'fraud_alert_channel', // (Optional) ตั้งชื่อ Channel สำหรับ Android
                sound: 'default'
            }
        },
        apns: {
            payload: {
                aps: {
                    sound: 'default',
                    contentAvailable: true
                }
            }
        }
    };
    
    try {
        const response = await admin.messaging().send(message);
        console.log('Successfully sent message:', response);
        return response;
    } catch (error) {
        console.error('Error sending message:', error);
        // ไม่ throw error เพื่อไม่ให้กระทบ Flow หลัก (แค่แจ้งเตือนไม่ไป แต่ Transaction ยังทำงานต่อ)
    }
};

module.exports = { sendPushNotification };