// routes/familyRoutes.js
const express = require('express');
const router = express.Router();
const familyController = require('../controllers/familyController');
const authMiddleware = require('../middlewares/authMiddleware');

// ทุก Route ในนี้ต้องผ่านการ Auth (มี Token) ก่อน
router.use(authMiddleware);

// สร้างครอบครัวใหม่
router.post('/', familyController.createFamily);

// ดึงรายชื่อสมาชิก (จริงๆ URL ควรเป็น /members เฉยๆ เพราะเราอยู่ภายใต้ /family แล้ว แต่ตาม req คือ /family/members ก็ได้ครับ)
router.get('/members', familyController.getMembers);

// เพิ่มสมาชิก
router.post('/members', familyController.addMember);

module.exports = router;