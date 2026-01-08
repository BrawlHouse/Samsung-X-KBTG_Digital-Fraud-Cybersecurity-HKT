const admin = require("firebase-admin");
const path = require("path");

if (!process.env.FIREBASE_KEY_PATH) {
  throw new Error("FIREBASE_KEY_PATH is not set");
}

const serviceAccount = require(
  path.resolve(process.env.FIREBASE_KEY_PATH)
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

module.exports = admin;