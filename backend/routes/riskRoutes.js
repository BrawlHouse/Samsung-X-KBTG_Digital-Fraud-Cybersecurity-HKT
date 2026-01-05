const express = require('express');
const router = express.Router();
const riskController = require('../controllers/riskController');
const authMiddleware = require('../middlewares/authMiddleware');

router.use(authMiddleware);

/**
 * @swagger
 * tags:
 *   - name: Risk
 *     description: ระบบวิเคราะห์ความเสี่ยง AI
 */

/**
 * @swagger
 * /risk/analyze:
 *   post:
 *     summary: วิเคราะห์ความเสี่ยงจากคำตอบ 5 ข้อ
 *     tags:
 *       - Risk
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
 *                 items:
 *                   type: string
 *                 example:
 *                   ["ไม่รู้", "ไม่รู้จัก", "ตำรวจ", "ให้โอนเงิน", "ด่วน"]
 *     responses:
 *       200:
 *         description: ผลวิเคราะห์ความเสี่ยง
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 risk_score:
 *                   type: integer
 *                   example: 85
 *                 level:
 *                   type: string
 *                   enum: [LOW, MEDIUM, HIGH]
 *                   example: HIGH
 *                 reasons:
 *                   type: array
 *                   items:
 *                     type: string
 *                   example:
 *                     ["มีการขอให้ทำธุรกรรมทางการเงิน", "มีการอ้างเป็นเจ้าหน้าที่รัฐ"]
 *       400:
 *         description: ส่งข้อมูลมาไม่ถูกต้อง (ไม่ใช่ Array)
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Please provide answers as an array
 */
router.post('/analyze', riskController.analyze);

module.exports = router;
