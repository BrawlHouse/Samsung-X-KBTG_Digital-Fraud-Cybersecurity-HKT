const admin = require("firebase-admin");

// 1. เรียกใช้ไฟล์ Service Account (key.json)
const serviceAccount = require("../firebase-service-account.json"); // <-- ตรวจสอบ Path ให้ถูกต้อง

// 2. Initialize App
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

// 3. สร้าง Payload ข้อความ
// ในการทดสอบจริง ต้องเอา FCM Token จากเครื่อง Android มาใส่ตรงนี้
const registrationToken = "chZExOTDSPyR49aZvFJDkc:APA91bEE89Iescr0Qa8_GPm9-7lfUoWfGuch8SMu9D2N2V2zM-pnIas0iisUHj6TyYuz4EavRnCy9fqSoUvNAv04J7hPiA7F98Jqh-fR0Ml7MaLDQQbu1cc";

const message = {
    // ส่วน Notification จะเด้งเป็น popup อัตโนมัติถ้าแอปอยู่ Background
    notification: {
        title: "สวัสดีครับ",
        body: "นี่คือการทดสอบส่งจาก Node.js"
    },
    // ส่วน Data (Optional) ใช้ส่งข้อมูลไป process ต่อในโค้ด (ทำงานตอน user กด noti หรือแอปเปิดอยู่)
    data: {
        score: "850",
        time: "2:45"
    },
    token: registrationToken
};

// 4. ส่งข้อความ
admin.messaging().send(message)
    .then((response) => {
        // Response คือ string message ID
        console.log("ส่งสำเร็จแล้วครับ Message ID:", response);
    })
    .catch((error) => {
        console.log("เกิดข้อผิดพลาด:", error);
    });