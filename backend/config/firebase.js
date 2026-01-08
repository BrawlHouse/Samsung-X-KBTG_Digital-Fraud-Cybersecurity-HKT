const admin = require("firebase-admin");
const path = require("path");

if (!process.env.FIREBASE_KEY_PATH) {
  throw new Error("FIREBASE_KEY_PATH is not set");
}

const serviceAccount = require(
  path.resolve(process.env.FIREBASE_KEY_PATH)
);

// เช็คก่อนว่า Initialize ไปหรือยัง เพื่อป้องกัน Error ซ้ำ
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
}

module.exports = admin;