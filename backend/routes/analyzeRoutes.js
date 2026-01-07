const express = require('express');
const router = express.Router();
const analyzeController = require('../controllers/analyzeController');
const verifyToken = require('../middlewares/authMiddleware');

/**
 * @swagger
 * tags:
 *   - name: Analyze
 *     description: วิเคราะห์ข้อความเพื่อประเมินความเสี่ยงการเป็นมิจฉาชีพ (Scam / Call Center)
 */

/**
 * @swagger
 * /analyze/message:
 *   post:
 *     summary: วิเคราะห์ข้อความว่าเป็นมิจฉาชีพหรือไม่ (ต้องใช้ JWT)
 *     description: >
 *       ใช้ AI วิเคราะห์ข้อความ เช่น SMS หรือแชท
 *       เพื่อประเมินความเสี่ยงการเป็นมิจฉาชีพ
 *     tags: [Analyze]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - input
 *             properties:
 *               input:
 *                 type: string
 *                 example: "คุณได้รับสิทธิ์กู้เงิน 50,000 บาท ดอกเบี้ยต่ำ คลิกลิงก์ bit.ly/xc99 เพื่อยืนยันทันที"
 *     responses:
 *       200:
 *         description: วิเคราะห์สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 percentage:
 *                   type: number
 *                   example: 95
 *                   description: ความน่าจะเป็น (%) ที่เป็นมิจฉาชีพ
 *                 level:
 *                   type: string
 *                   enum: [low, medium, high]
 *                   example: high
 *                 reason:
 *                   type: string
 *                   example: "มีการแนบลิงก์ย่อที่น่าสงสัย และเสนอเงินกู้โดยที่ผู้รับไม่ได้ร้องขอ"
 *       401:
 *         description: ไม่ได้ส่ง JWT หรือ Token ไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Unauthorized"
 *       400:
 *         description: Input ไม่ถูกต้อง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Input text is required"
 *       500:
 *         description: เกิดข้อผิดพลาดภายในระบบ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: "Failed to analyze message"
 */


// POST /analyze/message
router.post('/message', verifyToken, analyzeController.analyzeMessage);

module.exports = router;