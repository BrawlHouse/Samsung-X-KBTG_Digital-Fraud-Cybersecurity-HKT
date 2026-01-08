const admin = require("firebase-admin");
const serviceAccount = require("../firebase-service-account.json"); // อ้างอิงไฟล์ key ที่วางไว้

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

module.exports = admin;