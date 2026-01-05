const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const authMiddleware = require('../middlewares/authMiddleware'); 


/**
 * @swagger
 * /users:
 *   post:
 *     summary: ลงทะเบียนเข้าใช้งานครั้งแรก (Register / Login)
 *     tags:
 *       - Users
 *     security: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - nickname
 *               - device_id
 *               - role
 *             properties:
 *               nickname:
 *                 type: string
 *                 example: คุณแม่สมศรี
 *               device_id:
 *                 type: string
 *                 example: device_abc_123
 *               role:
 *                 type: string
 *                 enum: [parent, child]
 *                 example: parent
 *     responses:
 *       201:
 *         description: ลงทะเบียนสำเร็จ ได้รับ Token
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User registered successfully
 *                 user:
 *                   type: object
 *                   properties:
 *                     user_id:
 *                       type: integer
 *                       example: 1
 *                     nickname:
 *                       type: string
 *                       example: คุณแม่สมศรี
 *                     role:
 *                       type: string
 *                       example: parent
 *                     family_id:
 *                       type: integer
 *                       nullable: true
 *                       example: null
 *                     device_id:
 *                       type: string
 *                       example: device_abc_123
 *                     token:
 *                       type: string
 *                       description: JWT Token สำหรับใช้ยิง API อื่น
 *       400:
 *         description: ข้อมูลไม่ครบ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: Please provide nickname, device_id, and role
 */

/**
 * @swagger
 * /users/fcm-token:
 *   post:
 *     summary: อัปเดต FCM Token สำหรับรับแจ้งเตือน (ต้อง Login ก่อน)
 *     tags:
 *       - Users
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - token
 *             properties:
 *               token:
 *                 type: string
 *                 description: Device Token จาก Firebase
 *                 example: "fcm_token_string_here..."
 *     responses:
 *       200:
 *         description: บันทึก Token สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: FCM token updated successfully
 */


/**
 * @swagger
 * /users/{user_id}:
 *   get:
 *     summary: ดูข้อมูลส่วนตัว (Get User Info)
 *     tags:
 *       - Users
 *     security: []
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *           example: 1
 *     responses:
 *       200:
 *         description: พบข้อมูลผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 user_id:
 *                   type: integer
 *                   example: 1
 *                 nickname:
 *                   type: string
 *                   example: คุณแม่สมศรี
 *                 role:
 *                   type: string
 *                   example: parent
 *                 family_id:
 *                   type: integer
 *                   nullable: true
 *                   example: null
 *                 device_id:
 *                   type: string
 *                   example: device_abc_123
 *                 created_at:
 *                   type: string
 *                   format: date-time
 *                   example: 2024-01-05T12:00:00.000Z
 *       404:
 *         description: ไม่พบผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 *
 *   delete:
 *     summary: ลบผู้ใช้งานออกจากระบบ (Delete User)
 *     tags:
 *       - Users
 *     security: []
 *     parameters:
 *       - in: path
 *         name: user_id
 *         required: true
 *         schema:
 *           type: integer
 *           example: 1
 *     responses:
 *       200:
 *         description: ลบผู้ใช้สำเร็จ
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: User 1 deleted successfully
 *       404:
 *         description: ไม่พบผู้ใช้งาน
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 error:
 *                   type: string
 *                   example: User not found
 */

// Register (Public)
router.post('/', userController.register);

// Update FCM Token (Require Login) --> ต้องเอาไว้ก่อน /:user_id เพื่อกันสับสน
router.post('/fcm-token', authMiddleware, userController.updateFcmToken);

// Get Info & Delete (Public or Protected depending on logic)
router.get('/:user_id', userController.getUserInfo);
router.delete('/:user_id', userController.deleteUser);

module.exports = router;