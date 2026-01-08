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
 *     description: ระบบจัดการความเสี่ยงและบันทึกธุรกรรม
 */

/**
 * @swagger
 * /risk/analyze:
 *   post:
 *     summary: วิเคราะห์ความเสี่ยงจากคำตอบและบันทึกธุรกรรม
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
 *               - amount
 *               - destination
 *             properties:
 *               answers:
 *                 type: array
 *                 items:
 *                   type: string
 *                 example: ["ไม่รู้", "ไม่รู้จัก", "ตำรวจ", "ให้โอนเงิน", "ด่วน"]
 *               amount:
 *                 type: number
 *                 example: 5000
 *               destination:
 *                 type: string
 *                 example: "123-456-7890"
 *     responses:
 *       201:
 *         description: วิเคราะห์และบันทึกสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Transaction processed and risk analyzed
 *                 risk_score:
 *                   type: integer
 *                   example: 85
 *                 risk_level:
 *                   type: string
 *                   enum: [LOW, MEDIUM, HIGH]
 *                   example: HIGH
 *                 status:
 *                   type: string
 *                   enum: [normal, pending_approval, approved, rejected]
 *                   example: pending_approval
 *                 transaction:
 *                   type: object
 *                   properties:
 *                     transaction_id:
 *                       type: integer
 *                       example: 10
 *                     amount:
 *                       type: number
 *                       example: 5000
 *                     destination:
 *                       type: string
 *                       example: "123-456-7890"
 *       400:
 *         description: ข้อมูลไม่ครบหรือไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Please provide answers, amount, and destination
 *       500:
 *         description: Server Error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Internal Server Error
 */
router.post('/analyze', riskController.analyze);



/**
 * @swagger
 * /risk/respond:
 *   post:
 *     summary: ลูกหลานกดยอมรับ/ไม่ยอมรับให้ธุรกรรมพ่อแม่ (Approve/Reject)
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
 *                 example: 10
 *               action:
 *                 type: string
 *                 enum: [approve, reject]
 *                 example: approve
 *     responses:
 *       200:
 *         description: อัปเดตสถานะสำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: Transaction 10 approved successfully
 *                 transaction:
 *                   type: object
 *                   properties:
 *                     transaction_id:
 *                       type: integer
 *                       example: 10
 *                     status:
 *                       type: string
 *                       enum: [normal, pending_approval, approved, rejected]
 *                       example: approved
 *       403:
 *         description: ไม่มีสิทธิ์อนุมัติ (คนละบ้าน)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: You do not have permission to approve this transaction
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
 */
router.post('/respond', riskController.respondToTransaction);


module.exports = router;