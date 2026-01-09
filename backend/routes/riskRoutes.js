// routes/riskRoutes.js
const express = require('express');
const router = express.Router();
const riskController = require('../controllers/riskController');
const authMiddleware = require('../middlewares/authMiddleware');

// บังคับต้อง Login ก่อนใช้งาน
router.use(authMiddleware);

/**
 * @swagger
 * tags:
 *   - name: Risk
 *     description: ระบบวิเคราะห์ความเสี่ยงและการอนุมัติธุรกรรมของผู้สูงอายุ
 */

/**
 * @swagger
 * /risk/analyze:
 *   post:
 *     summary: วิเคราะห์ความเสี่ยงของธุรกรรม (เรียกโดยผู้สูงอายุ)
 *     description: |
 *       วิเคราะห์ความเสี่ยงจากคำตอบ (answers)
 *       - ระบบจะคำนวณ risk_score (0-100)
 *       - หาก risk_score >= 80 → สร้าง Transaction สถานะ `waiting`
 *       - แจ้งเตือนสมาชิกในครอบครัวผ่าน FCM
 *     tags: [Risk]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - answers
 *             properties:
 *               answers:
 *                 type: array
 *                 description: คำตอบ 5 ข้อ (Who, Relationship, Profession, Action, Urgency)
 *                 items:
 *                   type: string
 *                 example:
 *                   - "โทรมา"
 *                   - "ไม่รู้จัก"
 *                   - "ตำรวจ"
 *                   - "ให้โอนเงิน"
 *                   - "ข่มขู่"
 *               amount:
 *                 type: number
 *                 description: จำนวนเงิน (ถ้ามี)
 *                 example: 50000
 *               destination:
 *                 type: string
 *                 description: ปลายทางการโอน
 *                 example: "บัญชีธนาคาร XXX"
 *     responses:
 *       201:
 *         description: วิเคราะห์สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Analysis complete
 *                 ai_result:
 *                   type: object
 *                   properties:
 *                     risk_score:
 *                       type: number
 *                       example: 90
 *                     level:
 *                       type: string
 *                       enum: [LOW, HIGH]
 *                       example: HIGH
 *                     reasons:
 *                       type: array
 *                       items:
 *                         type: string
 *                       example:
 *                         - ติดต่อจากคนไม่รู้จัก
 *                         - มีการแอบอ้างเป็นตำรวจ
 *                 transaction:
 *                   type: object
 *                   properties:
 *                     transaction_id:
 *                       type: integer
 *                       example: 15
 *                     user_id:
 *                       type: integer
 *                       example: 3
 *                     amount:
 *                       type: number
 *                       example: 50000
 *                     destination:
 *                       type: string
 *                       example: "บัญชีธนาคาร XXX"
 *                     risk_score:
 *                       type: number
 *                       example: 90
 *                     status:
 *                       type: string
 *                       enum: [normal, waiting]
 *                       example: waiting
 *       400:
 *         description: Input ไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Invalid answers format
 *       500:
 *         description: Server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Server error
 */


router.post('/analyze', riskController.analyze);

/**
 * @swagger
 * /risk/respond:
 *   post:
 *     summary: ลูกหลานตอบกลับธุรกรรม (Approve / Reject)
 *     description: |
 *       ใช้สำหรับสมาชิกครอบครัวกดยืนยันธุรกรรม
 *       - ใช้ Database Transaction + Lock ป้องกัน race condition
 *       - เปลี่ยนสถานะ Transaction
 *       - เปลี่ยนสถานะ User
 *       - ส่ง Notification ให้ทุกฝ่าย
 *     tags: [Risk]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - transaction_id
 *               - action
 *             properties:
 *               transaction_id:
 *                 type: integer
 *                 example: 15
 *               action:
 *                 type: string
 *                 enum: [approve, reject]
 *                 example: approve
 *     responses:
 *       200:
 *         description: อัปเดตสถานะธุรกรรมสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Transaction approve successfully
 *                 transaction:
 *                   type: object
 *                   properties:
 *                     transaction_id:
 *                       type: integer
 *                       example: 15
 *                     status:
 *                       type: string
 *                       enum: [allow, rejected]
 *                       example: allow
 *       400:
 *         description: Action ไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Action must be approve or reject
 *       404:
 *         description: ไม่พบ Transaction
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Transaction not found
 *       409:
 *         description: รายการถูกจัดการไปแล้ว
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: รายการนี้ถูกจัดการไปแล้วโดยสมาชิกอื่น
 *                 current_status:
 *                   type: string
 *                   example: allow
 *       500:
 *         description: Server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Server error
 */
router.post('/respond', riskController.respondToTransaction);


module.exports = router;